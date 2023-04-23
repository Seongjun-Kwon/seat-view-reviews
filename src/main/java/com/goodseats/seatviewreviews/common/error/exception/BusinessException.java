package com.goodseats.seatviewreviews.common.error.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	protected HttpStatus status;
	protected String message;

	public BusinessException(HttpStatus status, String message) {
		super(message);
		this.status = status;
		this.message = message;
	}

	public BusinessException(HttpStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
		this.message = message;
	}
}
