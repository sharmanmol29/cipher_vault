package com.ciphervault.service;

import com.ciphervault.dto.file.request.ShareFileRequest;
import com.ciphervault.dto.file.response.FileItemResponse;
import com.ciphervault.entity.User;
import com.ciphervault.entity.VaultFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VaultFileService {

	List<FileItemResponse> listActive(User owner, Long folderId);

	List<FileItemResponse> listTrash(User owner);

	VaultFile upload(User owner, Long folderId, MultipartFile multipartFile) throws Exception;

	Resource downloadDecrypted(User owner, Long fileId);

	void softDelete(User owner, Long fileId);

	void restore(User owner, Long fileId);

	void permanentDelete(User owner, Long fileId);

	FileItemResponse configureShare(User owner, Long fileId, ShareFileRequest request);

	void purgeExpiredTrash();

	Resource downloadPublicShare(String token, String password);
}
