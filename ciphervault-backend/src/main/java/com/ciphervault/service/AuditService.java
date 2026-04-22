package com.ciphervault.service;

import com.ciphervault.entity.User;
import com.ciphervault.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {

	void log(User user, AuditAction action, String resourceType, Long resourceId, String details, String ipAddress);

	void logAnonymous(AuditAction action, String resourceType, Long resourceId, String details, String ipAddress);

	Page<com.ciphervault.entity.AuditLog> findAll(Pageable pageable);
}
