package com.ciphervault.service;

public interface EncryptionService {

	byte[] encryptFilePayload(long fileId, byte[] plaintext);

	byte[] decryptFilePayload(long fileId, byte[] ciphertextWithIv);
}
