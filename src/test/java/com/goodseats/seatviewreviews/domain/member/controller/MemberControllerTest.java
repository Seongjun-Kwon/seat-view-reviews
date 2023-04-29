package com.goodseats.seatviewreviews.domain.member.controller;

import static com.goodseats.seatviewreviews.common.security.SessionConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.member.model.dto.MemberLoginRequest;
import com.goodseats.seatviewreviews.domain.member.model.dto.MemberSignUpRequest;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	private MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(
			"goodseat@google.com",
			"password",
			"Jerome"
	);

	private MemberLoginRequest loginRequest = new MemberLoginRequest(
			memberSignUpRequest.loginEmail(),
			memberSignUpRequest.password()
	);

	@Test
	@DisplayName("Success - 회원가입에 성공한다")
	void signUpSuccess() throws Exception {

		// given & when & then
		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("loginEmail", memberSignUpRequest.loginEmail())
						.param("password", memberSignUpRequest.password())
						.param("nickname", memberSignUpRequest.nickname()))
				.andExpect(status().isCreated())
				.andDo(print());
	}

	@Nested
	@Transactional
	@DisplayName("signUpFail")
	class signUpFail {

		Member duplicateEmailMember = new Member(
				"duplicate@google.com", "password", "nickname"
		);
		Member duplicateNicknameMember = new Member(
				"goodseat@google.com", "password", "duplicate"
		);

		@BeforeEach
		void setUp() {
			memberRepository.save(duplicateEmailMember);
			memberRepository.save(duplicateNicknameMember);
		}

		@Test
		@DisplayName("Fail - 중복된 이메일로 회원가입하면 409 응답으로 실패한다")
		void signUpFailByDuplicateLoginEmail() throws Exception {
			// given
			MemberSignUpRequest duplicateEmailSignUpRequest = new MemberSignUpRequest(
					duplicateEmailMember.getLoginEmail(),
					duplicateEmailMember.getPassword(),
					duplicateEmailMember.getNickname()
			);

			// when & then
			mockMvc.perform(post("/api/v1/members")
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.param("loginEmail", duplicateEmailSignUpRequest.loginEmail())
							.param("password", duplicateEmailSignUpRequest.password())
							.param("nickname", duplicateEmailSignUpRequest.nickname()))
					.andExpect(status().isConflict())
					.andDo(print());
		}

		@Test
		@DisplayName("Fail - 중복된 닉네임으로 회원가입하면 409 응답으로 실패한다")
		void signUpFailByDuplicateNickname() throws Exception {
			// given
			MemberSignUpRequest duplicateNicknameSignUpRequest = new MemberSignUpRequest(
					duplicateNicknameMember.getLoginEmail(),
					duplicateNicknameMember.getPassword(),
					duplicateNicknameMember.getNickname()
			);

			// when & then
			mockMvc.perform(post("/api/v1/members")
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.param("loginEmail", duplicateNicknameSignUpRequest.loginEmail())
							.param("password", duplicateNicknameSignUpRequest.password())
							.param("nickname", duplicateNicknameSignUpRequest.nickname()))
					.andExpect(status().isConflict())
					.andDo(print());
		}
	}

	@Test
	@DisplayName("Success - 로그인에 성공하여 204 응답을 한다.")
	void loginSuccess() throws Exception {
		// given
		String encodedPassword = BCrypt.hashpw(memberSignUpRequest.password(), BCrypt.gensalt());
		Member member = new Member(memberSignUpRequest.loginEmail(), encodedPassword, memberSignUpRequest.nickname());
		memberRepository.save(member);

		// when & then
		mockMvc.perform(post("/api/v1/members/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("loginEmail", loginRequest.loginEmail())
						.param("password", loginRequest.password()))
				.andExpect(status().isNoContent())
				.andDo(print());
	}

	@Nested
	@DisplayName("loginFail")
	class LoginFail {

		@Test
		@DisplayName("Fail - 잘못된 아이디로 로그인에 실패하면 400 응답으로 실패한다.")
		void loginFailByWrongLoginEmail() throws Exception {
			// given
			String encodedPassword = BCrypt.hashpw(memberSignUpRequest.password(), BCrypt.gensalt());
			Member member = new Member(memberSignUpRequest.loginEmail(), encodedPassword, memberSignUpRequest.nickname());
			memberRepository.save(member);

			MemberLoginRequest wrongLoginEmailRequest = new MemberLoginRequest(
					"wrongLoginEmail@naver.com",
					loginRequest.password()
			);

			// when & then
			mockMvc.perform(post("/api/v1/members/login")
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.param("loginEmail", wrongLoginEmailRequest.loginEmail())
							.param("password", wrongLoginEmailRequest.password()))
					.andExpect(status().isBadRequest())
					.andDo(print());
		}

		@Test
		@DisplayName("Fail - 잘못된 비밀번호로 로그인에 실패하면 400 응답으로 실패한다.")
		void loginFailByWrongPassword() throws Exception {
			// given
			String encodedPassword = BCrypt.hashpw(memberSignUpRequest.password(), BCrypt.gensalt());
			Member member = new Member(memberSignUpRequest.loginEmail(), encodedPassword, memberSignUpRequest.nickname());
			memberRepository.save(member);

			MemberLoginRequest wrongPasswordRequest = new MemberLoginRequest(
					loginRequest.loginEmail(),
					"wrongPassword"
			);

			// when & then
			mockMvc.perform(post("/api/v1/members/login")
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.param("loginEmail", wrongPasswordRequest.loginEmail())
							.param("password", wrongPasswordRequest.password()))
					.andExpect(status().isBadRequest())
					.andDo(print());
		}
	}

	@Test
	@DisplayName("Success - 로그아웃에 성공하고 204 응답을 한다.")
	void logoutSuccess() throws Exception {
		// given
		AuthenticationDTO authenticationDTO = new AuthenticationDTO(1L, MemberAuthority.USER);
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(LOGIN_MEMBER_INFO, authenticationDTO);

		// when & then
		mockMvc.perform(post("/api/v1/members/logout")
						.session(session))
				.andExpect(status().isNoContent())
				.andDo(print());
		assertThat(session.isInvalid()).isTrue();
	}

	@Test
	@DisplayName("Fail - 비로그인 유저가 로그아웃 요청을 하면 401 응답으로 실패한다.")
	void logoutFailByNoLogin() throws Exception {
		// given
		MockHttpSession session = new MockHttpSession();

		// when & then
		mockMvc.perform(post("/api/v1/members/logout")
						.session(session))
				.andExpect(status().isUnauthorized())
				.andDo(print());
	}
}