package com.ciphervault.service;

import com.ciphervault.dto.auth.request.ForgotPasswordRequest;
import com.ciphervault.dto.auth.request.LoginRequest;
import com.ciphervault.dto.auth.request.RefreshTokenRequest;
import com.ciphervault.dto.auth.request.ResetPasswordRequest;
import com.ciphervault.dto.auth.request.SignupRequest;
import com.ciphervault.dto.auth.response.AuthTokenResponse;
import com.ciphervault.dto.auth.response.UserMeResponse;
import com.ciphervault.entity.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthService {

	void signup(SignupRequest request);

	AuthTokenResponse login(LoginRequest request, String ipAddress);

	void forgotPassword(ForgotPasswordRequest request);

	void resetPassword(ResetPasswordRequest request);

	AuthTokenResponse refresh(RefreshTokenRequest request);

	UserMeResponse me(User user);

	AuthTokenResponse issueTokensForOAuthUser(OAuth2User oauthUser);
}
