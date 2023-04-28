package com.goodseats.seatviewreviews.domain.member.service;

public interface PasswordEncoder {

	String encode(String password);

	boolean isMatch(String password, String encodedPassword);
}
