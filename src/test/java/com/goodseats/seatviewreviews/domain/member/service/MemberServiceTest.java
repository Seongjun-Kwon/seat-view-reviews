package com.goodseats.seatviewreviews.domain.member.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.domain.member.model.dto.MemberSignUpRequest;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private Member savedMember;

	@InjectMocks
	private MemberService memberService;

	private MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(
			"goodseat@google.com",
			"password",
			"Jerome"
	);

	@Test
	@DisplayName("Success - 회원가입에 성공한다")
	void signUpSuccess() {
		// given
		String encodedPassword = BCrypt.hashpw("password", BCrypt.gensalt());
		Long memberId = 1L;
		when(memberRepository.existsByLoginEmail(memberSignUpRequest.loginEmail())).thenReturn(false);
		when(memberRepository.existsByNickname(memberSignUpRequest.nickname())).thenReturn(false);
		when(passwordEncoder.encode(memberSignUpRequest.password())).thenReturn(encodedPassword);
		when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
		when(savedMember.getId()).thenReturn(memberId);

		// when
		Long savedMemberId = memberService.signUp(memberSignUpRequest);

		// then
		verify(memberRepository).existsByLoginEmail(memberSignUpRequest.loginEmail());
		verify(memberRepository).existsByNickname(memberSignUpRequest.nickname());
		verify(passwordEncoder).encode(memberSignUpRequest.password());
		verify(memberRepository).save(any(Member.class));
		verify(savedMember).getId();
		assertThat(savedMemberId).isEqualTo(memberId);
	}

	@Nested
	@DisplayName("signUp")
	class singUpFail {

		@Test
		@DisplayName("Fail - 중복된 로그인 이메일이 있어 회원가입에 실패한다")
		void signUpFailByDuplicateLoginEmail() {
			// given
			when(memberRepository.existsByLoginEmail(memberSignUpRequest.loginEmail())).thenReturn(true);

			// when & then
			assertThatThrownBy(() -> memberService.signUp(memberSignUpRequest))
					.isExactlyInstanceOf(DuplicatedException.class)
					.hasMessage(DUPLICATED_ID.getMessage());
			verify(memberRepository).existsByLoginEmail(memberSignUpRequest.loginEmail());
			verify(memberRepository, times(0)).existsByNickname(memberSignUpRequest.nickname());
			verify(passwordEncoder, times(0)).encode(memberSignUpRequest.password());
			verify(memberRepository, times(0)).save(any(Member.class));
			verify(savedMember, times(0)).getId();
		}

		@Test
		@DisplayName("Fail - 중복된 닉네임이 있어 회원가입에 실패한다")
		void signUpFailByDuplicateNickname() {
			// given
			when(memberRepository.existsByLoginEmail(memberSignUpRequest.loginEmail())).thenReturn(false);
			when(memberRepository.existsByNickname(memberSignUpRequest.nickname())).thenReturn(true);

			// when & then
			assertThatThrownBy(() -> memberService.signUp(memberSignUpRequest))
					.isExactlyInstanceOf(DuplicatedException.class)
					.hasMessage(DUPLICATED_NICKNAME.getMessage());
			verify(memberRepository).existsByLoginEmail(memberSignUpRequest.loginEmail());
			verify(memberRepository).existsByNickname(memberSignUpRequest.nickname());
			verify(passwordEncoder, times(0)).encode(memberSignUpRequest.password());
			verify(memberRepository, times(0)).save(any(Member.class));
			verify(savedMember, times(0)).getId();
		}
	}
}