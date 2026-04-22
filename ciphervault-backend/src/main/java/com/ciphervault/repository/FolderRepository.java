package com.ciphervault.repository;

import com.ciphervault.entity.Folder;
import com.ciphervault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

	List<Folder> findByOwner(User owner);

	List<Folder> findByOwnerAndParentIsNull(User owner);

	List<Folder> findByOwnerAndParent(User owner, Folder parent);

	Optional<Folder> findByIdAndOwner(Long id, User owner);

	boolean existsByOwnerAndParentAndNameIgnoreCase(User owner, Folder parent, String name);
}
