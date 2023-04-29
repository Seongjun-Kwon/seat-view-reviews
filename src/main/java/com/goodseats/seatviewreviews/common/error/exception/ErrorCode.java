package com.goodseats.seatviewreviews.common.error.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

	NOT_FOUND(404, "존재하지 않는 데이터입니다."),
	DUPLICATED_ID(409, "중복된 아이디입니다."),
	DUPLICATED_NICKNAME(409, "중복된 닉네임입니다."),
	UNAUTHORIZED(401, "인증되지 않은 사용자입니다."),
	BAD_LOGIN_REQUEST(400, "아이디 혹은 비밀번호가 틀립니다.");

	private final int status;
	private final String message;

	ErrorCode(int status, String message) {
		this.status = status;
		this.message = message;
	}
}
