package com.goodseats.seatviewreviews.domain.image.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodseats.seatviewreviews.common.TestUtils;
import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageDeleteRequest;
import com.goodseats.seatviewreviews.domain.image.model.entity.Image;
import com.goodseats.seatviewreviews.domain.image.model.vo.ImageType;
import com.goodseats.seatviewreviews.domain.image.repository.ImageRepository;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ImageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ImageRepository imageRepository;

	private MockMultipartFile multipartFile = new MockMultipartFile(
			"multipartFile", "testOriginalName.png", IMAGE_PNG_VALUE, "testContent".getBytes()
	);
	private ImageType imageType = ImageType.REVIEW;
	private Long referenceId = 1L;

	@Test
	@DisplayName("Success - 이미지 저장에 성공하고 200 으로 응답한다")
	void createImageSuccess() throws Exception {
		// given
		Member member = new Member("test@test.com", "test", "test");
		memberRepository.save(member);

		MockHttpSession session = TestUtils.getLoginSession(member, MemberAuthority.USER);

		// when & then
		mockMvc.perform(multipart("/api/v1/images")
						.file(multipartFile)
						.param("imageType", imageType.toString())
						.param("referenceId", referenceId.toString())
						.contentType(MULTIPART_FORM_DATA)
						.accept(APPLICATION_JSON)
						.session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("imageId").isNumber())
				.andExpect(jsonPath("imageUrl").isString())
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

			MockHttpSession session = TestUtils.getLoginSession(member, MemberAuthority.USER);

			MockMultipartFile multipartFile = new MockMultipartFile(
					"multipartFile", "testOriginalName.png", APPLICATION_PDF_VALUE, "testContent".getBytes()
			);

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
			// given & when & then
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

	@Test
	@DisplayName("Success - 이미지 단건 삭제에 성공하고 204 응답한다")
	void deleteImageSuccess() throws Exception {
		// given
		Member member = new Member("test@test.com", "test", "test");
		memberRepository.save(member);

		MockHttpSession session = TestUtils.getLoginSession(member, MemberAuthority.USER);

		Image image = new Image(ImageType.REVIEW, 1L, "testUrl", "테스트 이미지.jpg");
		Image savedImage = imageRepository.save(image);

		// when
		mockMvc.perform(delete("/api/v1/images/{imageId}", savedImage.getId())
						.session(session))
				.andExpect(status().isNoContent());

		// then
		assertThat(savedImage.getDeletedAt()).isNotNull();
	}

	@Nested
	@DisplayName("deleteImageFail")
	class DeleteImage {

		@Test
		@DisplayName("Fail - 삭제하고자 하는 이미지가 존재하지 않으면 실패하고 404 응답한다")
		void deleteImageFailByNotFound() throws Exception {
			// given
			Member member = new Member("test@test.com", "test", "test");
			memberRepository.save(member);

			MockHttpSession session = TestUtils.getLoginSession(member, MemberAuthority.USER);

			Long imageId = 1L;

			// when & then
			mockMvc.perform(delete("/api/v1/images/{imageId}", imageId)
							.session(session))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("Fail - 삭제하고자 하는 이미지가 이미 삭제되었으면 실패하고 409 응답한다")
		void deleteImageFailByAlreadyDeleted() throws Exception {
			// given
			Member member = new Member("test@test.com", "test", "test");
			memberRepository.save(member);

			MockHttpSession session = TestUtils.getLoginSession(member, MemberAuthority.USER);

			Image image = new Image(ImageType.REVIEW, 1L, "testUrl", "테스트 이미지.jpg");
			Image savedImage = imageRepository.save(image);
			image.delete();

			// when & then
			mockMvc.perform(delete("/api/v1/images/{imageId}", savedImage.getId())
							.session(session))
					.andExpect(status().isConflict());
		}
	}

	@Test
	@DisplayName("Success - 연관된 이미지들 삭제에 성공하고 204 응답한다")
	void deleteImagesSuccess() throws Exception {
		// given
		Member member = new Member("test@test.com", "test", "test");
		memberRepository.save(member);

		MockHttpSession session = TestUtils.getLoginSession(member, MemberAuthority.USER);

		Image image1 = new Image(imageType, referenceId, "testUrl1", "테스트 이미지1.jpg");
		Image image2 = new Image(imageType, referenceId, "testUrl2", "테스트 이미지2.jpg");
		List<Image> images = List.of(image1, image2);
		imageRepository.saveAll(images);

		ImageDeleteRequest imageDeleteRequest = new ImageDeleteRequest(imageType, referenceId);

		// when
		mockMvc.perform(delete("/api/v1/images")
						.session(session)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(imageDeleteRequest)))
				.andExpect(status().isNoContent());

		// then
		images.forEach(image -> assertThat(image.getDeletedAt()).isNotNull());
	}
}