package com.ciphervault.service;

import com.ciphervault.entity.User;

public interface EmailService {

	void sendPasswordResetEmail(User user, String rawToken);
}
