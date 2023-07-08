package com.goodseats.seatviewreviews.domain.image.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.common.file.FileStorageService;
import com.goodseats.seatviewreviews.domain.image.event.ImageDeleteEvent;
import com.goodseats.seatviewreviews.domain.image.event.RollbackUploadEvent;
import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageCreateRequest;
import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageDeleteRequest;
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

	private MockMultipartFile multipartFile = new MockMultipartFile(
			"testName", "testOriginalName", IMAGE_PNG_VALUE, "testContent".getBytes()
	);
	private ImageType imageType = ImageType.REVIEW;
	private Long referenceId = 1L;

	@Test
	@DisplayName("Success - 이미지 저장에 성공한다")
	void createImageSuccess() {
		// given
		ImageCreateRequest imageCreateRequest = new ImageCreateRequest(multipartFile, imageType, referenceId);

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
		ImageCreateRequest imageCreateRequest = new ImageCreateRequest(multipartFile, imageType, referenceId);

		// when & then
		assertThatThrownBy(() -> imageService.createImage(imageCreateRequest))
				.isExactlyInstanceOf(IllegalArgumentException.class)
				.hasMessage(BAD_IMAGE_REQUEST.getMessage());
	}

	@Test
	@DisplayName("Success - 이미지 단건 삭제에 성공한다")
	void deleteImageSuccess() {
		// given
		Long imageId = 1L;
		Image image = new Image(ImageType.REVIEW, 1L, "testUrl", "테스트 이미지.jpg");
		ReflectionTestUtils.setField(image, "id", imageId);

		when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
		doNothing().when(applicationEventPublisher).publishEvent(any(ImageDeleteEvent.class));

		// when
		imageService.deleteImage(imageId);

		// then
		assertThat(image.getDeletedAt()).isNotNull();
	}

	@Nested
	@DisplayName("deleteImageFail")
	class DeleteImage {

		@Test
		@DisplayName("Fail - 삭제하고자 하는 이미지가 존재하지 않으면 실패한다")
		void deleteImageFailByNotFound() {
			// given
			Long imageId = 1L;

			when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> imageService.deleteImage(imageId))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("Fail - 삭제하고자 하는 이미지가 이미 삭제되었으면 실패한다")
		void deleteImageFailByAlreadyDeleted() {
			// given
			Long imageId = 1L;
			Image image = new Image(ImageType.REVIEW, 1L, "testUrl", "테스트 이미지.jpg");
			ReflectionTestUtils.setField(image, "id", imageId);
			image.delete();

			when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

			// when & then
			assertThatThrownBy(() -> imageService.deleteImage(imageId))
					.isExactlyInstanceOf(DuplicatedException.class)
					.hasMessage(ALREADY_DELETED.getMessage());
		}
	}

	@Test
	@DisplayName("Success - 연관된 이미지들 삭제에 성공한다")
	void deleteImagesSuccess() {
		// given
		Image image1 = new Image(imageType, referenceId, "testUrl1", "테스트 이미지1.jpg");
		Image image2 = new Image(imageType, referenceId, "testUrl2", "테스트 이미지2.jpg");
		List<Image> images = List.of(image1, image2);

		ImageDeleteRequest imageDeleteRequest = new ImageDeleteRequest(imageType, referenceId);

		when(imageRepository.findAllByImageTypeAndReferenceIdAndDeletedAtIsNull(
				imageDeleteRequest.imageType(), imageDeleteRequest.referenceId()
		)).thenReturn(images);

		// when
		imageService.deleteImages(imageDeleteRequest);

		// then
		images.forEach(image -> assertThat(image.getDeletedAt()).isNotNull());
	}
}