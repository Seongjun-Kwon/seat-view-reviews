package com.goodseats.seatviewreviews.domain.member.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.common.error.exception.AuthenticationException;
import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.domain.member.mapper.MemberMapper;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.member.model.dto.MemberLoginRequest;
import com.goodseats.seatviewreviews.domain.member.model.dto.MemberSignUpRequest;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public Long signUp(MemberSignUpRequest memberSignUpRequest) {
		validateDuplicateEmail(memberSignUpRequest.loginEmail());
		validateDuplicateNickname(memberSignUpRequest.nickname());

		String encodedPassword = passwordEncoder.encode(memberSignUpRequest.password());
		Member savedMember = memberRepository.save(MemberMapper.toMember(memberSignUpRequest, encodedPassword));

		return savedMember.getId();
	}

	@Transactional(readOnly = true)
	public AuthenticationDTO login(MemberLoginRequest memberLoginRequest) {
		return memberRepository.findByLoginEmail(memberLoginRequest.loginEmail())
				.filter(member -> passwordEncoder.isMatch(memberLoginRequest.password(), member.getPassword()))
				.map(member -> new AuthenticationDTO(member.getId(), member.getMemberAuthority()))
				.orElseThrow(() -> new AuthenticationException(BAD_LOGIN_REQUEST));
	}

	private void validateDuplicateEmail(String loginEmail) {
		if (memberRepository.existsByLoginEmail(loginEmail)) {
			throw new DuplicatedException(DUPLICATED_ID);
		}
	}

	private void validateDuplicateNickname(String nickname) {
		if (memberRepository.existsByNickname(nickname)) {
			throw new DuplicatedException(DUPLICATED_NICKNAME);
		}
	}
}
