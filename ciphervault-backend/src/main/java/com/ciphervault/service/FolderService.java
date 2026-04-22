package com.ciphervault.service;

import com.ciphervault.dto.folder.request.CreateFolderRequest;
import com.ciphervault.dto.folder.response.FolderItemResponse;
import com.ciphervault.entity.User;

import java.util.List;

public interface FolderService {

	List<FolderItemResponse> list(User owner, Long parentId);

	FolderItemResponse create(User owner, CreateFolderRequest request);

	void delete(User owner, Long folderId);
}
