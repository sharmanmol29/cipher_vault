package com.ciphervault.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

	private Instant timestamp;
	private int status;
	private String error;
	private String code;
	private String message;
	private Map<String, String> fieldErrors;
}
