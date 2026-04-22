package com.ciphervault.service.impl;

import com.ciphervault.config.AppProperties;
import com.ciphervault.exception.ApiException;
import com.ciphervault.service.EncryptionService;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class AesGcmEncryptionService implements EncryptionService {

	private static final int GCM_IV_LENGTH = 12;
	private static final int GCM_TAG_BITS = 128;

	private final AppProperties appProperties;
	private final SecureRandom secureRandom = new SecureRandom();
	private byte[] masterKeyBytes;

	public AesGcmEncryptionService(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@PostConstruct
	void init() {
		masterKeyBytes = Base64.getDecoder().decode(appProperties.getEncryption().getMasterKeyBase64());
		if (masterKeyBytes.length != 32) {
			throw new IllegalStateException("app.encryption.master-key-base64 must decode to exactly 32 bytes (AES-256)");
		}
	}

	@Override
	public byte[] encryptFilePayload(long fileId, byte[] plaintext) {
		try {
			SecretKey aesKey = deriveKeyForFile(fileId);
			byte[] iv = new byte[GCM_IV_LENGTH];
			secureRandom.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
			byte[] ciphertext = cipher.doFinal(plaintext);
			ByteBuffer bb = ByteBuffer.allocate(iv.length + ciphertext.length);
			bb.put(iv);
			bb.put(ciphertext);
			return bb.array();
		} catch (GeneralSecurityException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "ENCRYPTION_ERROR", "Failed to encrypt file");
		}
	}

	@Override
	public byte[] decryptFilePayload(long fileId, byte[] ciphertextWithIv) {
		try {
			if (ciphertextWithIv.length < GCM_IV_LENGTH + 16) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CIPHER", "Ciphertext is too short");
			}
			byte[] iv = Arrays.copyOfRange(ciphertextWithIv, 0, GCM_IV_LENGTH);
			byte[] ct = Arrays.copyOfRange(ciphertextWithIv, GCM_IV_LENGTH, ciphertextWithIv.length);
			SecretKey aesKey = deriveKeyForFile(fileId);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
			return cipher.doFinal(ct);
		} catch (ApiException ex) {
			throw ex;
		} catch (GeneralSecurityException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "DECRYPTION_ERROR", "Failed to decrypt file");
		}
	}

	private SecretKey deriveKeyForFile(long fileId) throws GeneralSecurityException {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(masterKeyBytes, "HmacSHA256"));
		mac.update((byte) 0x01);
		mac.update(ByteBuffer.allocate(8).putLong(fileId).array());
		byte[] raw = mac.doFinal();
		byte[] keyBytes = Arrays.copyOf(raw, 32);
		return new SecretKeySpec(keyBytes, "AES");
	}
}
