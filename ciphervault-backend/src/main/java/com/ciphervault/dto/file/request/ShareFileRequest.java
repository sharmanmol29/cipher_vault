package com.ciphervault.dto.file.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShareFileRequest {

	@Size(max = 128)
	private String password;
}
