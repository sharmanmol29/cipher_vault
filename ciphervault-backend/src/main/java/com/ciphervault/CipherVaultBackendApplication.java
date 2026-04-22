package com.ciphervault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.ciphervault.config")
@EnableScheduling
public class CipherVaultBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CipherVaultBackendApplication.class, args);
	}
}
