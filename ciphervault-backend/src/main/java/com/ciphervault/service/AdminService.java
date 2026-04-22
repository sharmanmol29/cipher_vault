package com.ciphervault.service;

import com.ciphervault.dto.admin.request.ChangeRoleRequest;
import com.ciphervault.dto.admin.response.AuditLogRowResponse;
import com.ciphervault.dto.admin.response.UserAdminRowResponse;
import com.ciphervault.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

	Page<UserAdminRowResponse> listUsers(Pageable pageable);

	void deleteUser(Long userId);

	void changeRole(Long userId, Role role);

	Page<AuditLogRowResponse> listLogs(Pageable pageable);
}
