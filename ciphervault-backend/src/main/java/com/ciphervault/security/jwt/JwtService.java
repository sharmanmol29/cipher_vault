package com.ciphervault.security.jwt;

import com.ciphervault.config.AppProperties;
import com.ciphervault.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

	private final AppProperties appProperties;

	public JwtService(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	public String createAccessToken(User user) {
		Instant now = Instant.now();
		Instant exp = now.plusMillis(appProperties.getJwt().getAccessTokenValidityMs());
		return Jwts.builder()
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.claim("role", user.getRole().name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.signWith(signingKey())
				.compact();
	}

	public Long extractUserId(String token) {
		return Long.parseLong(parseClaims(token).getSubject());
	}

	public boolean isAccessTokenValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (ExpiredJwtException ex) {
			return false;
		} catch (MalformedJwtException ex) {
			return false;
		} catch (Exception ex) {
			log.debug("Invalid JWT", ex);
			return false;
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey signingKey() {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
			return Keys.hmacShaKeyFor(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
