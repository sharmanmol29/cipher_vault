package com.ciphervault.dto.folder.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateFolderRequest {

	private Long parentFolderId;

	@NotBlank
	@Size(max = 255)
	private String name;
}
