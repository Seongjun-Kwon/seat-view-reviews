package com.goodseats.seatviewreviews.domain.member.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.goodseats.seatviewreviews.common.error.exception.AuthenticationException;
import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.member.model.dto.request.MemberLoginRequest;
import com.goodseats.seatviewreviews.domain.member.model.dto.request.MemberSignUpRequest;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
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

	private MemberLoginRequest loginRequest = new MemberLoginRequest(
			memberSignUpRequest.loginEmail(),
			memberSignUpRequest.password()
	);

	private String encodedPassword = BCrypt.hashpw(memberSignUpRequest.password(), BCrypt.gensalt());
	private Long memberId = 1L;

	@Test
	@DisplayName("Success - 회원가입에 성공한다")
	void signUpSuccess() {
		// given
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
	@DisplayName("signUpFail")
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

	@Test
	@DisplayName("Success - 로그인에 성공한다.")
	void loginSuccess() {
		// given
		when(memberRepository.findByLoginEmail(loginRequest.loginEmail())).thenReturn(Optional.of(savedMember));
		when(savedMember.getPassword()).thenReturn(encodedPassword);
		when(passwordEncoder.isMatch(loginRequest.password(), encodedPassword)).thenReturn(true);
		when(savedMember.getId()).thenReturn(memberId);
		when(savedMember.getMemberAuthority()).thenReturn(MemberAuthority.USER);

		// when
		AuthenticationDTO authenticationDTO = memberService.login(loginRequest);

		// then
		verify(memberRepository).findByLoginEmail(loginRequest.loginEmail());
		verify(savedMember).getPassword();
		verify(passwordEncoder).isMatch(loginRequest.password(), encodedPassword);
		verify(savedMember).getId();
		verify(savedMember).getMemberAuthority();
		assertThat(authenticationDTO.memberId()).isEqualTo(memberId);
		assertThat(authenticationDTO.memberAuthority()).isEqualTo(MemberAuthority.USER);
	}

	@Nested
	@DisplayName("loginFail")
	class LoginFail {

		@Test
		@DisplayName("Fail - 잘못된 아이디를 입력하여 로그인에 실패한다.")
		void loginFailByWrongLoginEmail() {
			// given
			when(memberRepository.findByLoginEmail(loginRequest.loginEmail())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> memberService.login(loginRequest))
					.isExactlyInstanceOf(AuthenticationException.class)
					.hasMessage(BAD_LOGIN_REQUEST.getMessage());
			verify(memberRepository).findByLoginEmail(loginRequest.loginEmail());
		}

		@Test
		@DisplayName("Fail - 잘못된 비밀번호를 입력하여 로그인에 실패한다.")
		void loginFailByWrongPassword() {
			// given
			String wrongPassword = "wrongPassword";
			when(memberRepository.findByLoginEmail(loginRequest.loginEmail())).thenReturn(Optional.of(savedMember));
			when(savedMember.getPassword()).thenReturn(wrongPassword);
			when(passwordEncoder.isMatch(loginRequest.password(), wrongPassword)).thenReturn(false);

			// when & then
			assertThatThrownBy(() -> memberService.login(loginRequest))
					.isExactlyInstanceOf(AuthenticationException.class)
					.hasMessage(BAD_LOGIN_REQUEST.getMessage());
			verify(memberRepository).findByLoginEmail(loginRequest.loginEmail());
			verify(savedMember).getPassword();
			verify(passwordEncoder).isMatch(loginRequest.password(), wrongPassword);
		}
	}
}