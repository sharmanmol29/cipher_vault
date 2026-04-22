package com.ciphervault.service.impl;

import com.ciphervault.dto.file.request.ShareFileRequest;
import com.ciphervault.dto.file.response.FileItemResponse;
import com.ciphervault.entity.Folder;
import com.ciphervault.entity.User;
import com.ciphervault.entity.VaultFile;
import com.ciphervault.enums.AuditAction;
import com.ciphervault.exception.ApiException;
import com.ciphervault.exception.ConflictException;
import com.ciphervault.exception.ResourceNotFoundException;
import com.ciphervault.exception.UnauthorizedException;
import com.ciphervault.mapper.VaultFileMapper;
import com.ciphervault.repository.FolderRepository;
import com.ciphervault.repository.VaultFileRepository;
import com.ciphervault.repository.UserRepository;
import com.ciphervault.service.AuditService;
import com.ciphervault.service.EncryptionService;
import com.ciphervault.service.FileStorageService;
import com.ciphervault.service.VaultFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class VaultFileServiceImpl implements VaultFileService {

	private final VaultFileRepository vaultFileRepository;
	private final FolderRepository folderRepository;
	private final UserRepository userRepository;
	private final EncryptionService encryptionService;
	private final FileStorageService fileStorageService;
	private final AuditService auditService;
	private final VaultFileMapper vaultFileMapper;
	private final PasswordEncoder passwordEncoder;
	private final int trashRetentionDays;

	public VaultFileServiceImpl(
			VaultFileRepository vaultFileRepository,
			FolderRepository folderRepository,
			UserRepository userRepository,
			EncryptionService encryptionService,
			FileStorageService fileStorageService,
			AuditService auditService,
			VaultFileMapper vaultFileMapper,
			PasswordEncoder passwordEncoder,
			@Value("${app.storage.trash-retention-days:30}") int trashRetentionDays
	) {
		this.vaultFileRepository = vaultFileRepository;
		this.folderRepository = folderRepository;
		this.userRepository = userRepository;
		this.encryptionService = encryptionService;
		this.fileStorageService = fileStorageService;
		this.auditService = auditService;
		this.vaultFileMapper = vaultFileMapper;
		this.passwordEncoder = passwordEncoder;
		this.trashRetentionDays = trashRetentionDays;
	}

	@Override
	@Transactional(readOnly = true)
	public List<FileItemResponse> listActive(User owner, Long folderId) {
		if (folderId == null) {
			return vaultFileRepository.findByOwnerAndFolderIsNullAndTrashedAtIsNull(owner).stream().map(vaultFileMapper::toItem).toList();
		}
		Folder folder = folderRepository.findByIdAndOwner(folderId, owner)
				.orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
		return vaultFileRepository.findByOwnerAndFolderAndTrashedAtIsNull(owner, folder).stream().map(vaultFileMapper::toItem).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<FileItemResponse> listTrash(User owner) {
		return vaultFileRepository.findByOwnerAndTrashedAtIsNotNull(owner).stream().map(vaultFileMapper::toItem).toList();
	}

	@Override
	@Transactional
	public VaultFile upload(User owner, Long folderId, MultipartFile multipartFile) throws IOException {
		if (multipartFile.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "File is empty");
		}
		byte[] plaintext = multipartFile.getBytes();
		User managedOwner = userRepository.findById(owner.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		if (managedOwner.getStorageUsedBytes() + plaintext.length > managedOwner.getStorageLimitBytes()) {
			throw new ConflictException("Storage quota exceeded");
		}
		Folder folder = null;
		if (folderId != null) {
			folder = folderRepository.findByIdAndOwner(folderId, managedOwner)
					.orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
		}
		VaultFile entity = VaultFile.builder()
				.originalFilename(multipartFile.getOriginalFilename() != null ? multipartFile.getOriginalFilename() : "upload.bin")
				.contentType(multipartFile.getContentType())
				.plaintextSizeBytes(plaintext.length)
				.storageRelativePath("pending")
				.owner(managedOwner)
				.folder(folder)
				.build();
		entity = vaultFileRepository.save(entity);
		entity.setStorageRelativePath(entity.getId() + ".enc");
		entity = vaultFileRepository.save(entity);
		byte[] ciphertext = encryptionService.encryptFilePayload(entity.getId(), plaintext);
		fileStorageService.writeEncryptedFile(managedOwner.getId(), entity.getId(), ciphertext);
		managedOwner.setStorageUsedBytes(managedOwner.getStorageUsedBytes() + plaintext.length);
		userRepository.save(managedOwner);
		auditService.log(managedOwner, AuditAction.FILE_UPLOADED, "FILE", entity.getId(), entity.getOriginalFilename(), null);
		return entity;
	}

	@Override
	@Transactional
	public Resource downloadDecrypted(User owner, Long fileId) {
		VaultFile file = vaultFileRepository.findByIdAndOwner(fileId, owner)
				.orElseThrow(() -> new ResourceNotFoundException("File not found"));
		if (file.getTrashedAt() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_IN_TRASH", "Restore the file before downloading");
		}
		try {
			byte[] cipher = fileStorageService.readEncryptedFile(owner.getId(), file.getId());
			byte[] plain = encryptionService.decryptFilePayload(file.getId(), cipher);
			auditService.log(owner, AuditAction.FILE_DOWNLOADED, "FILE", file.getId(), file.getOriginalFilename(), null);
			return new ByteArrayResource(plain);
		} catch (IOException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_READ_ERROR", "Could not read file");
		}
	}

	@Override
	@Transactional
	public void softDelete(User owner, Long fileId) {
		VaultFile file = vaultFileRepository.findByIdAndOwner(fileId, owner)
				.orElseThrow(() -> new ResourceNotFoundException("File not found"));
		if (file.getTrashedAt() != null) {
			return;
		}
		file.setTrashedAt(Instant.now());
		vaultFileRepository.save(file);
		auditService.log(owner, AuditAction.FILE_DELETED, "FILE", file.getId(), file.getOriginalFilename(), null);
	}

	@Override
	@Transactional
	public void restore(User owner, Long fileId) {
		VaultFile file = vaultFileRepository.findByIdAndOwner(fileId, owner)
				.orElseThrow(() -> new ResourceNotFoundException("File not found"));
		file.setTrashedAt(null);
		vaultFileRepository.save(file);
		auditService.log(owner, AuditAction.FILE_RESTORED, "FILE", file.getId(), file.getOriginalFilename(), null);
	}

	@Override
	@Transactional
	public void permanentDelete(User owner, Long fileId) {
		VaultFile file = vaultFileRepository.findByIdAndOwner(fileId, owner)
				.orElseThrow(() -> new ResourceNotFoundException("File not found"));
		try {
			fileStorageService.deleteEncryptedFile(owner.getId(), file.getId());
		} catch (IOException ignored) {
		}
		User managedOwner = userRepository.findById(owner.getId()).orElseThrow();
		managedOwner.setStorageUsedBytes(Math.max(0, managedOwner.getStorageUsedBytes() - file.getPlaintextSizeBytes()));
		userRepository.save(managedOwner);
		vaultFileRepository.delete(file);
		auditService.log(owner, AuditAction.FILE_PURGED, "FILE", file.getId(), file.getOriginalFilename(), null);
	}

	@Override
	@Transactional
	public FileItemResponse configureShare(User owner, Long fileId, ShareFileRequest request) {
		VaultFile file = vaultFileRepository.findByIdAndOwner(fileId, owner)
				.orElseThrow(() -> new ResourceNotFoundException("File not found"));
		if (file.getTrashedAt() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_IN_TRASH", "Cannot share a trashed file");
		}
		String token = UUID.randomUUID().toString().replace("-", "");
		file.setShareToken(token);
		file.setShareExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			file.setSharePasswordHash(passwordEncoder.encode(request.getPassword()));
		} else {
			file.setSharePasswordHash(null);
		}
		vaultFileRepository.save(file);
		auditService.log(owner, AuditAction.SHARE_CREATED, "FILE", file.getId(), token, null);
		return vaultFileMapper.toItem(file);
	}

	@Override
	@Transactional
	public void purgeExpiredTrash() {
		Instant cutoff = Instant.now().minus(trashRetentionDays, ChronoUnit.DAYS);
		List<VaultFile> old = vaultFileRepository.findByTrashedAtBeforeAndTrashedAtIsNotNull(cutoff);
		for (VaultFile file : old) {
			try {
				fileStorageService.deleteEncryptedFile(file.getOwner().getId(), file.getId());
			} catch (IOException ignored) {
			}
			User owner = userRepository.findById(file.getOwner().getId()).orElse(null);
			if (owner != null) {
				owner.setStorageUsedBytes(Math.max(0, owner.getStorageUsedBytes() - file.getPlaintextSizeBytes()));
				userRepository.save(owner);
				auditService.log(owner, AuditAction.FILE_PURGED, "FILE", file.getId(), "Scheduled purge", null);
			}
			vaultFileRepository.delete(file);
		}
	}

	@Override
	@Transactional
	public Resource downloadPublicShare(String token, String password) {
		VaultFile file = vaultFileRepository.findByShareToken(token)
				.orElseThrow(() -> new ResourceNotFoundException("Share not found"));
		if (file.getShareExpiresAt() != null && file.getShareExpiresAt().isBefore(Instant.now())) {
			throw new UnauthorizedException("Share link expired");
		}
		if (file.getSharePasswordHash() != null) {
			if (password == null || !passwordEncoder.matches(password, file.getSharePasswordHash())) {
				throw new UnauthorizedException("Invalid share password");
			}
		}
		try {
			byte[] cipher = fileStorageService.readEncryptedFile(file.getOwner().getId(), file.getId());
			byte[] plain = encryptionService.decryptFilePayload(file.getId(), cipher);
			return new ByteArrayResource(plain);
		} catch (IOException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_READ_ERROR", "Could not read file");
		}
	}
}
