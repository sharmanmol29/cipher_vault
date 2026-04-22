package com.ciphervault.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class SecureTokenGenerator {

	private static final SecureRandom RANDOM = new SecureRandom();

	private SecureTokenGenerator() {
	}

	public static String randomUrlSafe(int byteLength) {
		byte[] buf = new byte[byteLength];
		RANDOM.nextBytes(buf);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
	}
}
