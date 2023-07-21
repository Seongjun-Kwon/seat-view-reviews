package com.goodseats.seatviewreviews.domain.member.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.common.TestUtils;
import com.goodseats.seatviewreviews.domain.member.model.dto.request.MemberLoginRequest;
import com.goodseats.seatviewreviews.domain.member.model.dto.request.MemberSignUpRequest;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
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
				.andDo(print())
				.andDo(document("회원가입 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
						requestParameters(
								parameterWithName("loginEmail").description("회원 로그인 이메일"),
								parameterWithName("password").description("로그인 비밀번호"),
								parameterWithName("nickname").description("회원 닉네임")
						)));
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
					.andDo(print())
					.andDo(document("회원가입 실패 - 중복된 이메일인 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestParameters(
									parameterWithName("loginEmail").description("회원 로그인 이메일"),
									parameterWithName("password").description("로그인 비밀번호"),
									parameterWithName("nickname").description("회원 닉네임")
							),
							responseFields(
									fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
									fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
									fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
									fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
									fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
									fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
							)));
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
					.andDo(print())
					.andDo(document("회원가입 실패 - 중복된 닉네임인 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestParameters(
									parameterWithName("loginEmail").description("회원 로그인 이메일"),
									parameterWithName("password").description("로그인 비밀번호"),
									parameterWithName("nickname").description("회원 닉네임")
							),
							responseFields(
									fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
									fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
									fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
									fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
									fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
									fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
							)));
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
				.andDo(print())
				.andDo(document("로그인 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
						requestParameters(
								parameterWithName("loginEmail").description("로그인 이메일"),
								parameterWithName("password").description("로그인 비밀번호")
						)));
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
					.andDo(print())
					.andDo(document("로그인 실패 - 잘못된 아이디인 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestParameters(
									parameterWithName("loginEmail").description("로그인 이메일"),
									parameterWithName("password").description("로그인 비밀번호")
							),
							responseFields(
									fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
									fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
									fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
									fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
									fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
									fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
							)));
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
					.andDo(print())
					.andDo(document("로그인 실패 - 잘못된 비밀번호인 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestParameters(
									parameterWithName("loginEmail").description("로그인 이메일"),
									parameterWithName("password").description("로그인 비밀번호")
							),
							responseFields(
									fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
									fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
									fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
									fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
									fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
									fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
							)));
		}
	}

	@Test
	@DisplayName("Success - 로그아웃에 성공하고 204 응답을 한다.")
	void logoutSuccess() throws Exception {
		// given
		String encodedPassword = BCrypt.hashpw(memberSignUpRequest.password(), BCrypt.gensalt());
		Member member = new Member(memberSignUpRequest.loginEmail(), encodedPassword, memberSignUpRequest.nickname());
		memberRepository.save(member);

		MockHttpSession session = TestUtils.getLoginSession(member, MemberAuthority.USER);

		// when & then
		mockMvc.perform(post("/api/v1/members/logout")
						.session(session))
				.andExpect(status().isNoContent())
				.andDo(print())
				.andDo(document("로그아웃 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint())));
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
				.andDo(print())
				.andDo(document("로그아웃 실패 - 비로그인 유저인 경우",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						responseFields(
								fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
								fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
								fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
								fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
								fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
								fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
						)));
	}
}