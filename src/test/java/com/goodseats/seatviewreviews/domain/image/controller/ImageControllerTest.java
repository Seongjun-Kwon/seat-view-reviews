package com.goodseats.seatviewreviews.domain.image.controller;

import static com.goodseats.seatviewreviews.common.security.SessionConstant.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.domain.image.model.vo.ImageType;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ImageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("Success - 이미지 저장에 성공하고 201 로 응답한다")
	void createImageSuccess() throws Exception {
		// given
		Member member = new Member("test@test.com", "test", "test");
		memberRepository.save(member);

		AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), MemberAuthority.USER);
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(LOGIN_MEMBER_INFO, authenticationDTO);

		MockMultipartFile multipartFile = new MockMultipartFile(
				"multipartFile", "testOriginalName.png", IMAGE_PNG_VALUE, "testContent".getBytes()
		);
		ImageType imageType = ImageType.REVIEW;
		Long referenceId = 1L;

		// when & then
		mockMvc.perform(multipart("/api/v1/images")
						.file(multipartFile)
						.param("imageType", imageType.toString())
						.param("referenceId", referenceId.toString())
						.contentType(MULTIPART_FORM_DATA)
						.accept(APPLICATION_JSON)
						.session(session))
				.andExpect(status().isCreated())
				.andDo(print());
	}

	@Nested
	@DisplayName("createImageFail")
	class createImageFail {
		@Test
		@DisplayName("Fail - 이미지가 아닌 파일 요청이 들어오면 저장에 실패하고 400 응답한다")
		void createImageFailByNotImageRequest() throws Exception {
			// given
			Member member = new Member("test@test.com", "test", "test");
			memberRepository.save(member);

			AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), MemberAuthority.USER);
			MockHttpSession session = new MockHttpSession();
			session.setAttribute(LOGIN_MEMBER_INFO, authenticationDTO);

			MockMultipartFile multipartFile = new MockMultipartFile(
					"multipartFile", "testOriginalName.png", APPLICATION_PDF_VALUE, "testContent".getBytes()
			);
			ImageType imageType = ImageType.REVIEW;
			Long referenceId = 1L;

			// when & then
			mockMvc.perform(multipart("/api/v1/images")
							.file(multipartFile)
							.param("imageType", imageType.toString())
							.param("referenceId", referenceId.toString())
							.contentType(MULTIPART_FORM_DATA)
							.accept(APPLICATION_JSON)
							.session(session))
					.andExpect(status().isBadRequest())
					.andDo(print());
		}

		@Test
		@DisplayName("Fail - 비회원이 이미지 저장을 요청하면 실패하고 401 응답한다")
		void createImageFailByUnauthorized() throws Exception {
			// given
			MockMultipartFile multipartFile = new MockMultipartFile(
					"multipartFile", "testOriginalName.png", APPLICATION_PDF_VALUE, "testContent".getBytes()
			);
			ImageType imageType = ImageType.REVIEW;
			Long referenceId = 1L;

			// when & then
			mockMvc.perform(multipart("/api/v1/images")
							.file(multipartFile)
							.param("imageType", imageType.toString())
							.param("referenceId", referenceId.toString())
							.contentType(MULTIPART_FORM_DATA)
							.accept(APPLICATION_JSON))
					.andExpect(status().isUnauthorized())
					.andDo(print());
		}
	}
}