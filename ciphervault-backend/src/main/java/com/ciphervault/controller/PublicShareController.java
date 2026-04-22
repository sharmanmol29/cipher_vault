package com.ciphervault.controller;

import com.ciphervault.dto.file.request.PublicShareDownloadRequest;
import com.ciphervault.entity.VaultFile;
import com.ciphervault.exception.ResourceNotFoundException;
import com.ciphervault.repository.VaultFileRepository;
import com.ciphervault.service.VaultFileService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/public/share")
public class PublicShareController {

	private final VaultFileRepository vaultFileRepository;
	private final VaultFileService vaultFileService;

	public PublicShareController(VaultFileRepository vaultFileRepository, VaultFileService vaultFileService) {
		this.vaultFileRepository = vaultFileRepository;
		this.vaultFileService = vaultFileService;
	}

	@GetMapping("/{token}/meta")
	public ResponseEntity<Map<String, Object>> meta(@PathVariable String token) {
		VaultFile file = vaultFileRepository.findByShareToken(token)
				.orElseThrow(() -> new ResourceNotFoundException("Share not found"));
		boolean passwordProtected = file.getSharePasswordHash() != null;
		boolean expired = file.getShareExpiresAt() != null && file.getShareExpiresAt().isBefore(Instant.now());
		return ResponseEntity.ok(Map.of(
				"filename", file.getOriginalFilename(),
				"contentType", file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE,
				"passwordProtected", passwordProtected,
				"expired", expired
		));
	}

	@PostMapping("/{token}/download")
	public ResponseEntity<Resource> download(
			@PathVariable String token,
			@Valid @RequestBody(required = false) PublicShareDownloadRequest request
	) {
		String password = request != null ? request.getPassword() : null;
		VaultFile file = vaultFileRepository.findByShareToken(token).orElseThrow();
		Resource resource = vaultFileService.downloadPublicShare(token, password);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFilename().replace("\"", "") + "\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}
}
