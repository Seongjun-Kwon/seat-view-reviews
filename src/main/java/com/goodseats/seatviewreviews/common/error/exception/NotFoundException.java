package com.goodseats.seatviewreviews.common.error.exception;

public class NotFoundException extends BusinessException{

	public NotFoundException(ErrorCode errorCode) {
		super(errorCode);
	}

	public NotFoundException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
