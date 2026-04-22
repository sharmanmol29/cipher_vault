package com.ciphervault.dto.auth.response;

import com.ciphervault.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMeResponse {

	private Long id;
	private String email;
	private String displayName;
	private Role role;
	private boolean emailVerified;
	private long storageUsedBytes;
	private long storageLimitBytes;
}
