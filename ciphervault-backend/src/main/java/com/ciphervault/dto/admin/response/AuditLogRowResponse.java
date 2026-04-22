package com.ciphervault.dto.admin.response;

import com.ciphervault.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRowResponse {

	private Long id;
	private Long userId;
	private String userEmail;
	private AuditAction action;
	private String resourceType;
	private Long resourceId;
	private String details;
	private String ipAddress;
	private Instant createdAt;
}
