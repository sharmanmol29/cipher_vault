package com.ciphervault.dto.folder.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderItemResponse {

	private Long id;
	private String name;
	private Long parentId;
	private Instant createdAt;
}
