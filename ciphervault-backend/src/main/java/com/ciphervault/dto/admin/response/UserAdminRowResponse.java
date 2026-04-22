package com.ciphervault.dto.admin.response;

import com.ciphervault.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminRowResponse {

	private Long id;
	private String email;
	private String displayName;
	private Role role;
	private boolean emailVerified;
	private long storageUsedBytes;
	private long storageLimitBytes;
	private Instant createdAt;
}
