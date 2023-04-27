package com.goodseats.seatviewreviews.domain.member.service;

import org.springframework.stereotype.Service;

import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.common.error.exception.ErrorCode;
import com.goodseats.seatviewreviews.domain.member.mapper.MemberMapper;
import com.goodseats.seatviewreviews.domain.member.model.dto.MemberSignUpRequest;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public Long signUp(MemberSignUpRequest memberSignUpRequest) {
		validateDuplicateEmail(memberSignUpRequest.loginEmail());
		validateDuplicateNickname(memberSignUpRequest.nickname());

		String encodedPassword = passwordEncoder.encode(memberSignUpRequest.password());
		Member savedMember = memberRepository.save(MemberMapper.toMember(memberSignUpRequest, encodedPassword));

		return savedMember.getId();
	}

	private void validateDuplicateEmail(String loginEmail) {
		if (memberRepository.existsByLoginEmail(loginEmail)) {
			throw new DuplicatedException(ErrorCode.DUPLICATED_ID);
		}
	}

	private void validateDuplicateNickname(String nickname) {
		if (memberRepository.existsByNickname(nickname)) {
			throw new DuplicatedException(ErrorCode.DUPLICATED_NICKNAME);
		}
	}
}
