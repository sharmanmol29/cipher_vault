package com.ciphervault.service.impl;

import com.ciphervault.config.AppProperties;
import com.ciphervault.entity.User;
import com.ciphervault.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmtpEmailService implements EmailService {

	private final JavaMailSender mailSender;
	private final AppProperties appProperties;

	public SmtpEmailService(JavaMailSender mailSender, AppProperties appProperties) {
		this.mailSender = mailSender;
		this.appProperties = appProperties;
	}

	@Override
	public void sendPasswordResetEmail(User user, String rawToken) {
		String link = appProperties.getFrontend().getBaseUrl().replaceAll("/$", "")
				+ "/reset-password?token=" + rawToken;
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(appProperties.getMail().getFromAddress());
		message.setTo(user.getEmail());
		message.setSubject("Reset your CipherVault password");
		message.setText("Hi " + user.getDisplayName() + ",\n\nReset your password using this link:\n" + link + "\n");
		send(message);
	}

	private void send(SimpleMailMessage message) {
		try {
			mailSender.send(message);
		} catch (Exception ex) {
			log.warn("Email send failed (check MAIL_* settings): {}", ex.getMessage());
		}
	}
}
