package com.ciphervault.service.impl;

import com.ciphervault.config.AppProperties;
import com.ciphervault.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalFileStorageService implements FileStorageService {

	private final AppProperties appProperties;
	private Path rootPath;

	public LocalFileStorageService(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@PostConstruct
	void init() throws IOException {
		rootPath = Paths.get(appProperties.getStorage().getRootDir()).toAbsolutePath().normalize();
		Files.createDirectories(rootPath);
	}

	@Override
	public Path resolveUserRoot(Long userId) {
		return rootPath.resolve(String.valueOf(userId)).normalize();
	}

	@Override
	public void writeEncryptedFile(Long userId, Long fileId, byte[] encryptedBytes) throws IOException {
		Path dir = resolveUserRoot(userId);
		Files.createDirectories(dir);
		Path target = dir.resolve(fileId + ".enc");
		Files.write(target, encryptedBytes);
	}

	@Override
	public byte[] readEncryptedFile(Long userId, Long fileId) throws IOException {
		Path target = resolveUserRoot(userId).resolve(fileId + ".enc");
		return Files.readAllBytes(target);
	}

	@Override
	public void deleteEncryptedFile(Long userId, Long fileId) throws IOException {
		Path target = resolveUserRoot(userId).resolve(fileId + ".enc");
		Files.deleteIfExists(target);
	}

	@Override
	public InputStream newInputStream(Long userId, Long fileId) throws IOException {
		Path target = resolveUserRoot(userId).resolve(fileId + ".enc");
		return Files.newInputStream(target);
	}
}
