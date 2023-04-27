package com.goodseats.seatviewreviews.domain.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.domain.member.model.dto.MemberSignUpRequest;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("Success - 회원가입에 성공한다")
	void signUpSuccess() throws Exception {
		// given
		MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(
				"goodseat@google.com",
				"password",
				"Jerome"
		);

		// when & then
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
}