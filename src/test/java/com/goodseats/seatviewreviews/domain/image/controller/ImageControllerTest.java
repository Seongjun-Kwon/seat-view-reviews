package com.goodseats.seatviewreviews.domain.image.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
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
@AutoConfigureRestDocs
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
				.andDo(print())
				.andDo(document("이미지 저장 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(
								headerWithName("Content-type").description("요청 타입 정보"),
								headerWithName("Accept").description("가능한 응답 타입 정보")
						),
						requestParts(partWithName("multipartFile").description("이미지 파일")),
						requestParameters(
								parameterWithName("imageType").description("연관된 엔티티 타입"),
								parameterWithName("referenceId").description("연관된 엔티티 id")
						),
						responseFields(
								fieldWithPath("imageId").type(JsonFieldType.NUMBER).description("이미지 id"),
								fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("이미지에 접근 가능한 url")
						)));
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
					.andDo(print())
					.andDo(document("이미지 저장 실패 - 이미지 파일이 아닌 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(
									headerWithName("Content-type").description("요청 타입 정보"),
									headerWithName("Accept").description("가능한 응답 타입 정보")
							),
							requestParts(partWithName("multipartFile").description("이미지 파일")),
							requestParameters(
									parameterWithName("imageType").description("연관된 엔티티 타입"),
									parameterWithName("referenceId").description("연관된 엔티티 id")
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
					.andDo(print())
					.andDo(document("이미지 저장 실패 - 비회원이 요청한 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(
									headerWithName("Content-type").description("요청 타입 정보"),
									headerWithName("Accept").description("가능한 응답 타입 정보")
							),
							requestParts(partWithName("multipartFile").description("이미지 파일")),
							requestParameters(
									parameterWithName("imageType").description("연관된 엔티티 타입"),
									parameterWithName("referenceId").description("연관된 엔티티 id")
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
	@DisplayName("Success - 이미지 단건 삭제에 성공하고 204 응답한다")
	void deleteImageSuccess() throws Exception {
		// given
		Member member = new Member("test@test.com", "test", "test");
		memberRepository.save(member);

		MockHttpSession session = TestUtils.getLoginSession(member, MemberAuthority.USER);

		Image image = new Image(ImageType.REVIEW, 1L, "testUrl", "테스트 이미지.jpg");
		Image savedImage = imageRepository.save(image);

		// when
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/v1/images/{imageId}", savedImage.getId())
						.session(session))
				.andExpect(status().isNoContent())
				.andDo(print())
				.andDo(document("이미지 단건 삭제 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						pathParameters(parameterWithName("imageId").description("삭제하려는 이미지 id"))));

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
			mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/v1/images/{imageId}", imageId)
							.session(session))
					.andExpect(status().isNotFound())
					.andDo(print())
					.andDo(document("이미지 단건 삭제 실패 - 이미지 id 없는 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							pathParameters(parameterWithName("imageId").description("삭제하려는 이미지 id")),
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
			mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/v1/images/{imageId}", savedImage.getId())
							.session(session))
					.andExpect(status().isConflict())
					.andDo(print())
					.andDo(document("이미지 단건 삭제 실패 - 이미 삭제된 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							pathParameters(parameterWithName("imageId").description("삭제하려는 이미지 id")),
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
				.andExpect(status().isNoContent())
				.andDo(print())
				.andDo(document("연관된 이미지 목록 삭제 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
						requestFields(
								fieldWithPath("imageType").type(JsonFieldType.STRING).description("연관된 엔티티 타입"),
								fieldWithPath("referenceId").type(JsonFieldType.NUMBER).description("연관된 엔티티 id")
						)));

		// then
		images.forEach(image -> assertThat(image.getDeletedAt()).isNotNull());
	}
}