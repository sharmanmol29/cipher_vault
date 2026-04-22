package com.ciphervault.controller;

import com.ciphervault.dto.auth.request.ForgotPasswordRequest;
import com.ciphervault.dto.auth.request.LoginRequest;
import com.ciphervault.dto.auth.request.RefreshTokenRequest;
import com.ciphervault.dto.auth.request.ResetPasswordRequest;
import com.ciphervault.dto.auth.request.SignupRequest;
import com.ciphervault.dto.auth.response.AuthTokenResponse;
import com.ciphervault.dto.auth.response.UserMeResponse;
import com.ciphervault.entity.User;
import com.ciphervault.repository.UserRepository;
import com.ciphervault.security.user.CustomUserDetails;
import com.ciphervault.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;

	public AuthController(AuthService authService, UserRepository userRepository) {
		this.authService = authService;
		this.userRepository = userRepository;
	}

	@PostMapping("/signup")
	public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
		authService.signup(request);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/login")
	public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
		return ResponseEntity.ok(authService.login(request, clientIp(http)));
	}

	@GetMapping("/oauth")
	public ResponseEntity<Map<String, String>> oauthInfo() {
		return ResponseEntity.ok(Map.of(
				"googleAuthorizationPath", "/oauth2/authorization/google"
		));
	}

	@PostMapping("/forgot")
	public ResponseEntity<Void> forgot(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/reset")
	public ResponseEntity<Void> reset(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ResponseEntity.ok(authService.refresh(request));
	}

	@GetMapping("/me")
	public ResponseEntity<UserMeResponse> me(@AuthenticationPrincipal CustomUserDetails principal) {
		User user = userRepository.findById(principal.getId()).orElseThrow();
		return ResponseEntity.ok(authService.me(user));
	}

	private static String clientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
