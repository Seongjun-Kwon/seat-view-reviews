package com.goodseats.seatviewreviews.common.util;

import static com.goodseats.seatviewreviews.common.constant.CookieConstant.*;

import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtils {

	private static final int DAY = 60 * 60 * 24;

	public static Cookie setUserKey(Cookie userKey, HttpServletResponse response) {
		if (Objects.nonNull(userKey)) {
			response.addCookie(userKey);
			return userKey;
		}

		userKey = new Cookie(USER_KEY, String.valueOf(UUID.randomUUID()));
		userKey.setPath("/");
		userKey.setMaxAge(DAY);
		userKey.setSecure(true);
		userKey.setHttpOnly(true);

		response.addCookie(userKey);

		return userKey;
	}
}
