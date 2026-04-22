package com.ciphervault.security.oauth2;

import com.ciphervault.config.AppProperties;
import com.ciphervault.service.AuthService;
import org.springframework.context.annotation.Lazy;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthService authService;
	private final AppProperties appProperties;

	public OAuth2LoginSuccessHandler(@Lazy AuthService authService, AppProperties appProperties) {
		this.authService = authService;
		this.appProperties = appProperties;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
		var tokens = authService.issueTokensForOAuthUser(oauthUser);
		String base = appProperties.getFrontend().getBaseUrl().replaceAll("/$", "");
		String redirect = base + "/oauth/callback"
				+ "?accessToken=" + url(tokens.getAccessToken())
				+ "&refreshToken=" + url(tokens.getRefreshToken())
				+ "&expiresInMs=" + tokens.getExpiresInMs();
		getRedirectStrategy().sendRedirect(request, response, redirect);
	}

	private static String url(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
