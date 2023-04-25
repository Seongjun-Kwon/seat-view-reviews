package com.goodseats.seatviewreviews.common.error.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

	NOT_FOUND(404, "존재하지 않는 데이터입니다.");

	private final int status;
	private final String message;

	ErrorCode(int status, String message) {
		this.status = status;
		this.message = message;
	}
}
