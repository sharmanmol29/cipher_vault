package com.ciphervault.security.jwt;

import com.ciphervault.repository.UserRepository;
import com.ciphervault.security.user.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		if (path.startsWith("/oauth2/")
				|| path.startsWith("/login/oauth2")
				|| path.startsWith("/api/public/")
				|| path.startsWith("/actuator/health")
				|| path.equals("/error")) {
			return true;
		}
		return path.equals("/api/auth/signup")
				|| path.equals("/api/auth/login")
				|| path.equals("/api/auth/oauth")
				|| path.equals("/api/auth/forgot")
				|| path.equals("/api/auth/reset")
				|| path.equals("/api/auth/refresh");
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String token = header.substring(7);
		if (!jwtService.isAccessTokenValid(token)) {
			filterChain.doFilter(request, response);
			return;
		}
		Long userId = jwtService.extractUserId(token);
		userRepository.findById(userId).ifPresent(user -> {
			CustomUserDetails principal = CustomUserDetails.fromUser(user);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					principal,
					null,
					principal.getAuthorities()
			);
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		});
		filterChain.doFilter(request, response);
	}
}
