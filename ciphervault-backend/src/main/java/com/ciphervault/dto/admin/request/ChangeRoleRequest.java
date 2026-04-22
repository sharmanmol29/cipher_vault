package com.ciphervault.dto.admin.request;

import com.ciphervault.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {

	@NotNull
	private Role role;
}
