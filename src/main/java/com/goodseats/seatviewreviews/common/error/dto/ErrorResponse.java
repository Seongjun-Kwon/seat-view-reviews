package com.goodseats.seatviewreviews.common.error.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ErrorResponse(
		int status,
		String url,
		String exceptionName,
		String message,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime createdAt,
		List<FieldErrorResponse> fieldErrors
) {
	public static ErrorResponse of(
			int status,
			String url,
			String exceptionName,
			String message,
			List<FieldErrorResponse> fieldErrors
	) {
		return new ErrorResponse(status, url, exceptionName, message, LocalDateTime.now(), fieldErrors);
	}
}
