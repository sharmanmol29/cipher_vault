package com.ciphervault.service.impl;

import com.ciphervault.dto.admin.response.AuditLogRowResponse;
import com.ciphervault.dto.admin.response.UserAdminRowResponse;
import com.ciphervault.entity.Folder;
import com.ciphervault.entity.User;
import com.ciphervault.enums.AuditAction;
import com.ciphervault.enums.Role;
import com.ciphervault.exception.ApiException;
import com.ciphervault.exception.ResourceNotFoundException;
import com.ciphervault.mapper.AuditLogMapper;
import com.ciphervault.mapper.UserMapper;
import com.ciphervault.repository.FolderRepository;
import com.ciphervault.repository.UserRepository;
import com.ciphervault.repository.VaultFileRepository;
import com.ciphervault.service.FileStorageService;
import com.ciphervault.service.AdminService;
import com.ciphervault.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

	private final UserRepository userRepository;
	private final VaultFileRepository vaultFileRepository;
	private final FolderRepository folderRepository;
	private final FileStorageService fileStorageService;
	private final UserMapper userMapper;
	private final AuditService auditService;
	private final AuditLogMapper auditLogMapper;

	public AdminServiceImpl(
			UserRepository userRepository,
			VaultFileRepository vaultFileRepository,
			FolderRepository folderRepository,
			FileStorageService fileStorageService,
			UserMapper userMapper,
			AuditService auditService,
			AuditLogMapper auditLogMapper
	) {
		this.userRepository = userRepository;
		this.vaultFileRepository = vaultFileRepository;
		this.folderRepository = folderRepository;
		this.fileStorageService = fileStorageService;
		this.userMapper = userMapper;
		this.auditService = auditService;
		this.auditLogMapper = auditLogMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserAdminRowResponse> listUsers(Pageable pageable) {
		return userRepository.findAll(pageable).map(userMapper::toAdminRow);
	}

	@Override
	@Transactional
	public void deleteUser(Long userId) {
		User actor = currentActor();
		if (actor.getId().equals(userId)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "SELF_DELETE", "You cannot delete your own account");
		}
		User target = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		for (var file : vaultFileRepository.findByOwner(target)) {
			try {
				fileStorageService.deleteEncryptedFile(target.getId(), file.getId());
			} catch (IOException ignored) {
			}
			vaultFileRepository.delete(file);
		}
		deleteFoldersTree(target);
		userRepository.delete(target);
		auditService.log(actor, AuditAction.ADMIN_USER_DELETED, "USER", userId, target.getEmail(), null);
	}

	@Override
	@Transactional
	public void changeRole(Long userId, Role role) {
		User actor = currentActor();
		User target = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		if (actor.getId().equals(userId)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "SELF_ROLE", "You cannot change your own role here");
		}
		target.setRole(role);
		userRepository.save(target);
		auditService.log(actor, AuditAction.ADMIN_ROLE_CHANGED, "USER", userId, role.name(), null);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AuditLogRowResponse> listLogs(Pageable pageable) {
		return auditService.findAll(pageable).map(auditLogMapper::toRow);
	}

	private User currentActor() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private void deleteFoldersTree(User owner) {
		List<Folder> remaining = folderRepository.findByOwner(owner);
		while (!remaining.isEmpty()) {
			boolean deletedAny = false;
			for (Folder folder : List.copyOf(remaining)) {
				if (folderRepository.findByOwnerAndParent(owner, folder).isEmpty()) {
					folderRepository.delete(folder);
					deletedAny = true;
				}
			}
			if (!deletedAny) {
				break;
			}
			remaining = folderRepository.findByOwner(owner);
		}
	}
}
