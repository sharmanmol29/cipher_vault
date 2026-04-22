package com.ciphervault.controller;

import com.ciphervault.dto.file.request.ShareFileRequest;
import com.ciphervault.dto.file.response.FileItemResponse;
import com.ciphervault.entity.User;
import com.ciphervault.entity.VaultFile;
import com.ciphervault.repository.UserRepository;
import com.ciphervault.repository.VaultFileRepository;
import com.ciphervault.security.user.CustomUserDetails;
import com.ciphervault.service.VaultFileService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class VaultFileController {

	private final VaultFileService vaultFileService;
	private final UserRepository userRepository;
	private final VaultFileRepository vaultFileRepository;

	public VaultFileController(VaultFileService vaultFileService, UserRepository userRepository, VaultFileRepository vaultFileRepository) {
		this.vaultFileService = vaultFileService;
		this.userRepository = userRepository;
		this.vaultFileRepository = vaultFileRepository;
	}

	@GetMapping
	public ResponseEntity<List<FileItemResponse>> list(
			@AuthenticationPrincipal CustomUserDetails principal,
			@RequestParam(required = false) Long folderId
	) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		return ResponseEntity.ok(vaultFileService.listActive(user, folderId));
	}

	@GetMapping("/trash")
	public ResponseEntity<List<FileItemResponse>> trash(@AuthenticationPrincipal CustomUserDetails principal) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		return ResponseEntity.ok(vaultFileService.listTrash(user));
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<FileItemResponse> upload(
			@AuthenticationPrincipal CustomUserDetails principal,
			@RequestParam("file") MultipartFile file,
			@RequestParam(required = false) Long folderId
	) throws Exception {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		VaultFile saved = vaultFileService.upload(user, folderId, file);
		VaultFile managed = vaultFileRepository.findById(saved.getId()).orElseThrow();
		return ResponseEntity.ok(
				FileItemResponse.builder()
						.id(managed.getId())
						.originalFilename(managed.getOriginalFilename())
						.contentType(managed.getContentType())
						.plaintextSizeBytes(managed.getPlaintextSizeBytes())
						.folderId(managed.getFolder() != null ? managed.getFolder().getId() : null)
						.createdAt(managed.getCreatedAt())
						.trashedAt(managed.getTrashedAt())
						.shareToken(managed.getShareToken())
						.shareExpiresAt(managed.getShareExpiresAt())
						.build()
		);
	}

	@GetMapping("/download/{id}")
	public ResponseEntity<Resource> download(
			@AuthenticationPrincipal CustomUserDetails principal,
			@PathVariable Long id
	) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		VaultFile meta = vaultFileRepository.findByIdAndOwner(id, user).orElseThrow();
		Resource resource = vaultFileService.downloadDecrypted(user, id);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getOriginalFilename().replace("\"", "") + "\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> softDelete(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		vaultFileService.softDelete(user, id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/restore/{id}")
	public ResponseEntity<Void> restore(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		vaultFileService.restore(user, id);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/purge/{id}")
	public ResponseEntity<Void> purge(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		vaultFileService.permanentDelete(user, id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/share/{id}")
	public ResponseEntity<FileItemResponse> share(
			@AuthenticationPrincipal CustomUserDetails principal,
			@PathVariable Long id,
			@Valid @RequestBody(required = false) ShareFileRequest request
	) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		ShareFileRequest body = request != null ? request : new ShareFileRequest();
		return ResponseEntity.ok(vaultFileService.configureShare(user, id, body));
	}
}
