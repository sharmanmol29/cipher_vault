package com.ciphervault.dto.file.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileItemResponse {

	private Long id;
	private String originalFilename;
	private String contentType;
	private long plaintextSizeBytes;
	private Long folderId;
	private Instant createdAt;
	private Instant trashedAt;
	private String shareToken;
	private Instant shareExpiresAt;
}
