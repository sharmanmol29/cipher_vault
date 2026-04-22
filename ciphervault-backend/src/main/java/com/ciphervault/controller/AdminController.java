package com.ciphervault.controller;

import com.ciphervault.dto.admin.request.ChangeRoleRequest;
import com.ciphervault.dto.admin.response.AuditLogRowResponse;
import com.ciphervault.dto.admin.response.UserAdminRowResponse;
import com.ciphervault.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@GetMapping("/users")
	public ResponseEntity<Page<UserAdminRowResponse>> users(@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(adminService.listUsers(pageable));
	}

	@DeleteMapping("/users/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		adminService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/users/{id}/role")
	public ResponseEntity<Void> changeRole(@PathVariable Long id, @Valid @RequestBody ChangeRoleRequest request) {
		adminService.changeRole(id, request.getRole());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/logs")
	public ResponseEntity<Page<AuditLogRowResponse>> logs(@PageableDefault(size = 50) Pageable pageable) {
		return ResponseEntity.ok(adminService.listLogs(pageable));
	}
}
