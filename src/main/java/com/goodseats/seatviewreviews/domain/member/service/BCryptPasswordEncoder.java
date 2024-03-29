package com.goodseats.seatviewreviews.domain.member.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoder implements PasswordEncoder{

	@Override
	public String encode(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}

	@Override
	public boolean isMatch(String password, String encodedPassword) {
		return BCrypt.checkpw(password, encodedPassword);
	}
}
