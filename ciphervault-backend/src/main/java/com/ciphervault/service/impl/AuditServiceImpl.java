package com.ciphervault.service.impl;

import com.ciphervault.entity.AuditLog;
import com.ciphervault.entity.User;
import com.ciphervault.enums.AuditAction;
import com.ciphervault.repository.AuditLogRepository;
import com.ciphervault.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditServiceImpl implements AuditService {

	private final AuditLogRepository auditLogRepository;

	public AuditServiceImpl(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	@Override
	@Transactional
	public void log(User user, AuditAction action, String resourceType, Long resourceId, String details, String ipAddress) {
		auditLogRepository.save(AuditLog.builder()
				.user(user)
				.action(action)
				.resourceType(resourceType)
				.resourceId(resourceId)
				.details(details)
				.ipAddress(ipAddress)
				.build());
	}

	@Override
	@Transactional
	public void logAnonymous(AuditAction action, String resourceType, Long resourceId, String details, String ipAddress) {
		auditLogRepository.save(AuditLog.builder()
				.user(null)
				.action(action)
				.resourceType(resourceType)
				.resourceId(resourceId)
				.details(details)
				.ipAddress(ipAddress)
				.build());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AuditLog> findAll(Pageable pageable) {
		return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
	}
}
