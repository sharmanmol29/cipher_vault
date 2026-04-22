package com.ciphervault.service.impl;

import com.ciphervault.config.AppProperties;
import com.ciphervault.dto.auth.request.ForgotPasswordRequest;
import com.ciphervault.dto.auth.request.LoginRequest;
import com.ciphervault.dto.auth.request.RefreshTokenRequest;
import com.ciphervault.dto.auth.request.ResetPasswordRequest;
import com.ciphervault.dto.auth.request.SignupRequest;
import com.ciphervault.dto.auth.response.AuthTokenResponse;
import com.ciphervault.dto.auth.response.UserMeResponse;
import com.ciphervault.entity.PasswordResetToken;
import com.ciphervault.entity.RefreshToken;
import com.ciphervault.entity.User;
import com.ciphervault.enums.AuditAction;
import com.ciphervault.enums.Role;
import com.ciphervault.exception.ApiException;
import com.ciphervault.exception.ConflictException;
import com.ciphervault.exception.ResourceNotFoundException;
import com.ciphervault.exception.UnauthorizedException;
import com.ciphervault.mapper.UserMapper;
import com.ciphervault.repository.PasswordResetTokenRepository;
import com.ciphervault.repository.RefreshTokenRepository;
import com.ciphervault.repository.UserRepository;
import com.ciphervault.security.jwt.JwtService;
import com.ciphervault.security.user.CustomUserDetails;
import com.ciphervault.service.AuditService;
import com.ciphervault.service.AuthService;
import com.ciphervault.service.EmailService;
import com.ciphervault.util.SecureTokenGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final ObjectProvider<AuthenticationManager> authenticationManager;
	private final JwtService jwtService;
	private final AppProperties appProperties;
	private final EmailService emailService;
	private final AuditService auditService;
	private final UserMapper userMapper;

	public AuthServiceImpl(
			UserRepository userRepository,
			PasswordResetTokenRepository passwordResetTokenRepository,
			RefreshTokenRepository refreshTokenRepository,
			PasswordEncoder passwordEncoder,
			ObjectProvider<AuthenticationManager> authenticationManager,
			JwtService jwtService,
			AppProperties appProperties,
			EmailService emailService,
			AuditService auditService,
			UserMapper userMapper
	) {
		this.userRepository = userRepository;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.appProperties = appProperties;
		this.emailService = emailService;
		this.auditService = auditService;
		this.userMapper = userMapper;
	}

	@Override
	@Transactional
	public void signup(SignupRequest request) {
		if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
			throw new ConflictException("Email is already registered");
		}
		User user = User.builder()
				.email(request.getEmail().trim().toLowerCase())
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.displayName(request.getDisplayName().trim())
				.role(Role.USER)
				.emailVerified(true)
				.storageUsedBytes(0L)
				.storageLimitBytes(appProperties.getStorage().getDefaultQuotaBytes())
				.build();
		user = userRepository.save(user);
		auditService.log(user, AuditAction.SIGNUP, "USER", user.getId(), null, null);
	}

	@Override
	@Transactional
	public AuthTokenResponse login(LoginRequest request, String ipAddress) {
		try {
			var auth = authenticationManager.getObject().authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
			);
			CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
			User user = userRepository.findById(principal.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
			auditService.log(user, AuditAction.LOGIN_SUCCESS, "USER", user.getId(), null, ipAddress);
			return issueTokens(user);
		} catch (BadCredentialsException ex) {
			auditService.logAnonymous(AuditAction.LOGIN_FAILURE, "USER", null, "Failed login for " + request.getEmail(), ipAddress);
			throw ex;
		}
	}

	@Override
	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {
		Optional<User> userOpt = userRepository.findByEmailIgnoreCase(request.getEmail().trim().toLowerCase());
		if (userOpt.isEmpty()) {
			return;
		}
		User user = userOpt.get();
		if (user.getPasswordHash() == null) {
			return;
		}
		String raw = SecureTokenGenerator.randomUrlSafe(32);
		PasswordResetToken token = PasswordResetToken.builder()
				.token(raw)
				.user(user)
				.expiresAt(Instant.now().plusSeconds(appProperties.getSecurity().getResetTokenValidityHours() * 3600L))
				.consumed(false)
				.build();
		passwordResetTokenRepository.save(token);
		emailService.sendPasswordResetEmail(user, raw);
		auditService.log(user, AuditAction.PASSWORD_RESET_REQUESTED, "USER", user.getId(), null, null);
	}

	@Override
	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
				.orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));
		if (token.isConsumed() || token.getExpiresAt().isBefore(Instant.now())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "TOKEN_INVALID", "Reset token is invalid or expired");
		}
		User user = token.getUser();
		user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
		token.setConsumed(true);
		passwordResetTokenRepository.save(token);
		auditService.log(user, AuditAction.PASSWORD_RESET_COMPLETED, "USER", user.getId(), null, null);
	}

	@Override
	@Transactional
	public AuthTokenResponse refresh(RefreshTokenRequest request) {
		RefreshToken existing = refreshTokenRepository.findByTokenAndRevokedIsFalse(request.getRefreshToken())
				.orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
		if (existing.getExpiresAt().isBefore(Instant.now())) {
			throw new UnauthorizedException("Refresh token expired");
		}
		existing.setRevoked(true);
		refreshTokenRepository.save(existing);
		return issueTokens(existing.getUser());
	}

	@Override
	@Transactional(readOnly = true)
	public UserMeResponse me(User user) {
		return userMapper.toMe(userRepository.findById(user.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found")));
	}

	@Override
	@Transactional
	public AuthTokenResponse issueTokensForOAuthUser(OAuth2User oauthUser) {
		String sub = oauthUser.getAttribute("sub");
		String email = oauthUser.getAttribute("email");
		String name = oauthUser.getAttribute("name");
		if (email == null || email.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "OAUTH_EMAIL_MISSING", "Google account email is required");
		}
		String normalizedEmail = email.trim().toLowerCase();
		User user = userRepository.findByGoogleSubject(sub)
				.orElseGet(() -> userRepository.findByEmailIgnoreCase(normalizedEmail)
						.map(existing -> {
							existing.setGoogleSubject(sub);
							existing.setEmailVerified(true);
							if ((existing.getDisplayName() == null || existing.getDisplayName().isBlank()) && name != null && !name.isBlank()) {
								existing.setDisplayName(name.trim());
							}
							return userRepository.save(existing);
						})
						.orElseGet(() -> userRepository.save(User.builder()
								.email(normalizedEmail)
								.displayName((name != null && !name.isBlank()) ? name.trim() : normalizedEmail.split("@")[0])
								.role(Role.USER)
								.emailVerified(true)
								.googleSubject(sub)
								.storageUsedBytes(0L)
								.storageLimitBytes(appProperties.getStorage().getDefaultQuotaBytes())
								.build())));
		auditService.log(user, AuditAction.OAUTH_LOGIN, "USER", user.getId(), "Google OAuth login", null);
		return issueTokens(user);
	}

	private AuthTokenResponse issueTokens(User user) {
		String refreshPlain = SecureTokenGenerator.randomUrlSafe(48);
		RefreshToken refreshToken = RefreshToken.builder()
				.token(refreshPlain)
				.user(user)
				.expiresAt(Instant.now().plusMillis(appProperties.getJwt().getRefreshTokenValidityMs()))
				.revoked(false)
				.build();
		refreshTokenRepository.save(refreshToken);
		String access = jwtService.createAccessToken(user);
		return AuthTokenResponse.builder()
				.accessToken(access)
				.refreshToken(refreshPlain)
				.expiresInMs(appProperties.getJwt().getAccessTokenValidityMs())
				.tokenType("Bearer")
				.build();
	}
}
