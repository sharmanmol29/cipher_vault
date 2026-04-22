package com.ciphervault.service.impl;

import com.ciphervault.dto.folder.request.CreateFolderRequest;
import com.ciphervault.dto.folder.response.FolderItemResponse;
import com.ciphervault.entity.Folder;
import com.ciphervault.entity.User;
import com.ciphervault.exception.ConflictException;
import com.ciphervault.exception.ResourceNotFoundException;
import com.ciphervault.mapper.FolderMapper;
import com.ciphervault.repository.FolderRepository;
import com.ciphervault.repository.VaultFileRepository;
import com.ciphervault.service.FolderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FolderServiceImpl implements FolderService {

	private final FolderRepository folderRepository;
	private final VaultFileRepository vaultFileRepository;
	private final FolderMapper folderMapper;

	public FolderServiceImpl(FolderRepository folderRepository, VaultFileRepository vaultFileRepository, FolderMapper folderMapper) {
		this.folderRepository = folderRepository;
		this.vaultFileRepository = vaultFileRepository;
		this.folderMapper = folderMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public List<FolderItemResponse> list(User owner, Long parentId) {
		if (parentId == null) {
			return folderRepository.findByOwnerAndParentIsNull(owner).stream().map(folderMapper::toItem).toList();
		}
		Folder parent = folderRepository.findByIdAndOwner(parentId, owner)
				.orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
		return folderRepository.findByOwnerAndParent(owner, parent).stream().map(folderMapper::toItem).toList();
	}

	@Override
	@Transactional
	public FolderItemResponse create(User owner, CreateFolderRequest request) {
		Folder parent = null;
		if (request.getParentFolderId() != null) {
			parent = folderRepository.findByIdAndOwner(request.getParentFolderId(), owner)
					.orElseThrow(() -> new ResourceNotFoundException("Parent folder not found"));
		}
		if (folderRepository.existsByOwnerAndParentAndNameIgnoreCase(owner, parent, request.getName().trim())) {
			throw new ConflictException("Folder already exists");
		}
		Folder folder = Folder.builder()
				.name(request.getName().trim())
				.parent(parent)
				.owner(owner)
				.build();
		folder = folderRepository.save(folder);
		return folderMapper.toItem(folder);
	}

	@Override
	@Transactional
	public void delete(User owner, Long folderId) {
		Folder folder = folderRepository.findByIdAndOwner(folderId, owner)
				.orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
		boolean hasChildren = !folderRepository.findByOwnerAndParent(owner, folder).isEmpty();
		if (hasChildren) {
			throw new ConflictException("Folder is not empty");
		}
		if (vaultFileRepository.existsByOwnerAndFolder(owner, folder)) {
			throw new ConflictException("Folder still contains files (including items in recycle bin). Empty the folder or delete those files first.");
		}
		folderRepository.delete(folder);
	}
}
