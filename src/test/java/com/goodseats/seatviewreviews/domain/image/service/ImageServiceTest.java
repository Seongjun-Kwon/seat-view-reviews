package com.goodseats.seatviewreviews.domain.image.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.goodseats.seatviewreviews.common.file.FileStorageService;
import com.goodseats.seatviewreviews.domain.image.event.RollbackUploadEvent;
import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageCreateRequest;
import com.goodseats.seatviewreviews.domain.image.model.dto.response.ImageCreateResponse;
import com.goodseats.seatviewreviews.domain.image.model.entity.Image;
import com.goodseats.seatviewreviews.domain.image.model.vo.ImageType;
import com.goodseats.seatviewreviews.domain.image.repository.ImageRepository;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@Mock
	private ImageRepository imageRepository;

	@Mock
	private FileStorageService fileStorageService;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@InjectMocks
	private ImageService imageService;

	@Test
	@DisplayName("Success - 이미지 저장에 성공한다")
	void createImageSuccess() {
		// given
		MockMultipartFile multipartFile = new MockMultipartFile(
				"testName", "testOriginalName", IMAGE_PNG_VALUE, "testContent".getBytes()
		);
		Long referenceId = 1L;
		ImageType imageType = ImageType.REVIEW;
		ImageCreateRequest imageCreateRequest = new ImageCreateRequest(multipartFile, ImageType.REVIEW, referenceId);

		Long imageId = 1L;
		String imageUrl = "testUrl";
		Image image = new Image(imageType, referenceId, imageUrl, multipartFile.getOriginalFilename());
		ReflectionTestUtils.setField(image, "id", imageId);

		when(fileStorageService.upload(imageCreateRequest.multipartFile(), imageCreateRequest.imageType().getSubPath()))
				.thenReturn(imageUrl);
		doNothing().when(applicationEventPublisher).publishEvent(any(RollbackUploadEvent.class));
		when(imageRepository.save(any(Image.class))).thenReturn(image);

		// when
		ImageCreateResponse ImageCreateResponse = imageService.createImage(imageCreateRequest);

		// then
		verify(fileStorageService).upload(imageCreateRequest.multipartFile(), imageCreateRequest.imageType().getSubPath());
		verify(applicationEventPublisher).publishEvent(any(RollbackUploadEvent.class));
		verify(imageRepository).save(any(Image.class));
		assertThat(ImageCreateResponse.imageId()).isEqualTo(image.getId());
		assertThat(ImageCreateResponse.imageUrl()).isEqualTo(image.getImageUrl());
	}

	@Test
	@DisplayName("Fail - 이미지가 아닌 파일 요청이 들어오면 저장에 실패한다")
	void createImageFailByNotImageRequest() {
		// given
		MockMultipartFile multipartFile = new MockMultipartFile(
				"testName", "testOriginalName", APPLICATION_PDF_VALUE, "testContent".getBytes()
		);
		Long referenceId = 1L;
		ImageType imageType = ImageType.REVIEW;
		ImageCreateRequest imageCreateRequest = new ImageCreateRequest(multipartFile, imageType, referenceId);

		// when & then
		assertThatThrownBy(() -> imageService.createImage(imageCreateRequest))
				.isExactlyInstanceOf(IllegalArgumentException.class)
				.hasMessage(BAD_IMAGE_REQUEST.getMessage());
	}
}