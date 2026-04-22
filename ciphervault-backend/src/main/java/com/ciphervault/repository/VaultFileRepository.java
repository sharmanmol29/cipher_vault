package com.ciphervault.repository;

import com.ciphervault.entity.Folder;
import com.ciphervault.entity.User;
import com.ciphervault.entity.VaultFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VaultFileRepository extends JpaRepository<VaultFile, Long> {

	List<VaultFile> findByOwner(User owner);

	List<VaultFile> findByOwnerAndFolderIsNullAndTrashedAtIsNull(User owner);

	List<VaultFile> findByOwnerAndFolderAndTrashedAtIsNull(User owner, Folder folder);

	boolean existsByOwnerAndFolder(User owner, Folder folder);

	List<VaultFile> findByOwnerAndTrashedAtIsNotNull(User owner);

	Optional<VaultFile> findByIdAndOwner(Long id, User owner);

	Optional<VaultFile> findByShareToken(String shareToken);

	@Query("select coalesce(sum(f.plaintextSizeBytes), 0) from VaultFile f where f.owner = :owner and f.trashedAt is null")
	long sumPlaintextSizeByOwnerActive(@Param("owner") User owner);

	List<VaultFile> findByTrashedAtBeforeAndTrashedAtIsNotNull(Instant cutoff);
}
