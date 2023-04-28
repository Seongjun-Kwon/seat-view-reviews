package com.goodseats.seatviewreviews.common.error.exception;

public class AuthenticationException extends BusinessException{

	public AuthenticationException(ErrorCode errorCode) {
		super(errorCode);
	}

	public AuthenticationException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
