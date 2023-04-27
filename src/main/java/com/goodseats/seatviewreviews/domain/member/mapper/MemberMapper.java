package com.goodseats.seatviewreviews.domain.member.mapper;

import com.goodseats.seatviewreviews.domain.member.model.dto.MemberSignUpRequest;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMapper {

	public static Member toMember(MemberSignUpRequest request, String encodedPassword) {
		return new Member(
				request.loginEmail(),
				encodedPassword,
				request.nickname()
		);
	}
}
