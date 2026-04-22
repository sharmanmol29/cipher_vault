package com.ciphervault.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private final Frontend frontend = new Frontend();
	private final Cors cors = new Cors();
	private final Jwt jwt = new Jwt();
	private final Encryption encryption = new Encryption();
	private final Storage storage = new Storage();
	private final Mail mail = new Mail();
	private final Security security = new Security();

	@Getter
	@Setter
	public static class Frontend {
		@NotBlank
		private String baseUrl = "http://localhost:5173";
	}

	@Getter
	@Setter
	public static class Cors {
		@NotBlank
		private String allowedOrigins = "http://localhost:5173";
	}

	@Getter
	@Setter
	public static class Jwt {
		@NotBlank
		private String secret;
		@Min(1000)
		private long accessTokenValidityMs = 900_000L;
		@Min(1000)
		private long refreshTokenValidityMs = 604_800_000L;
	}

	@Getter
	@Setter
	public static class Encryption {
		@NotBlank
		private String masterKeyBase64;
	}

	@Getter
	@Setter
	public static class Storage {
		@NotBlank
		private String rootDir = "./data/storage";
		@Min(1024)
		private long defaultQuotaBytes = 1_073_741_824L;
		@Min(1)
		private int trashRetentionDays = 30;
	}

	@Getter
	@Setter
	public static class Mail {
		@NotBlank
		private String fromAddress = "noreply@ciphervault.local";
	}

	@Getter
	@Setter
	public static class Security {
		@Min(1)
		private int resetTokenValidityHours = 24;
	}
}
