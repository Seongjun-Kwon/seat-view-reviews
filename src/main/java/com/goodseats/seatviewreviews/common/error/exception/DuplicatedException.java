package com.goodseats.seatviewreviews.common.error.exception;

public class DuplicatedException extends BusinessException{

	public DuplicatedException(ErrorCode errorCode) {
		super(errorCode);
	}

	public DuplicatedException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
