package com.ciphervault.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface FileStorageService {

	Path resolveUserRoot(Long userId);

	void writeEncryptedFile(Long userId, Long fileId, byte[] encryptedBytes) throws IOException;

	byte[] readEncryptedFile(Long userId, Long fileId) throws IOException;

	void deleteEncryptedFile(Long userId, Long fileId) throws IOException;

	InputStream newInputStream(Long userId, Long fileId) throws IOException;
}
