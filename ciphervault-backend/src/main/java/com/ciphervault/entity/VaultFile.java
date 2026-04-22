package com.ciphervault.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "vault_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaultFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "original_filename", nullable = false, length = 512)
	private String originalFilename;

	@Column(name = "content_type", length = 255)
	private String contentType;

	@Column(name = "plaintext_size_bytes", nullable = false)
	private long plaintextSizeBytes;

	@Column(name = "storage_relative_path", nullable = false, length = 1024)
	private String storageRelativePath;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "folder_id")
	private Folder folder;

	@Column(name = "trashed_at")
	private Instant trashedAt;

	@Column(name = "share_token", unique = true, length = 64)
	private String shareToken;

	@Column(name = "share_password_hash", length = 255)
	private String sharePasswordHash;

	@Column(name = "share_expires_at")
	private Instant shareExpiresAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
