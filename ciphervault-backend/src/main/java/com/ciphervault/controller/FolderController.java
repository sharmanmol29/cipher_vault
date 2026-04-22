package com.ciphervault.controller;

import com.ciphervault.dto.folder.request.CreateFolderRequest;
import com.ciphervault.dto.folder.response.FolderItemResponse;
import com.ciphervault.entity.User;
import com.ciphervault.repository.UserRepository;
import com.ciphervault.security.user.CustomUserDetails;
import com.ciphervault.service.FolderService;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

	private final FolderService folderService;
	private final UserRepository userRepository;

	public FolderController(FolderService folderService, UserRepository userRepository) {
		this.folderService = folderService;
		this.userRepository = userRepository;
	}

	@GetMapping
	public ResponseEntity<List<FolderItemResponse>> list(
			@AuthenticationPrincipal CustomUserDetails principal,
			@RequestParam(required = false) Long parentId
	) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		return ResponseEntity.ok(folderService.list(user, parentId));
	}

	@PostMapping("/create")
	public ResponseEntity<FolderItemResponse> create(
			@AuthenticationPrincipal CustomUserDetails principal,
			@Valid @RequestBody CreateFolderRequest request
	) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		return ResponseEntity.ok(folderService.create(user, request));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		folderService.delete(user, id);
		return ResponseEntity.noContent().build();
	}
}
