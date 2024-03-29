package com.goodseats.seatviewreviews.domain.image.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.image.event.ImageDeleteEvent;
import com.goodseats.seatviewreviews.domain.image.event.RollbackUploadEvent;
import com.goodseats.seatviewreviews.domain.image.mapper.ImageMapper;
import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageCreateRequest;
import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageDeleteRequest;
import com.goodseats.seatviewreviews.domain.image.model.dto.response.ImageCreateResponse;
import com.goodseats.seatviewreviews.domain.image.model.entity.Image;
import com.goodseats.seatviewreviews.domain.image.repository.ImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final ImageRepository imageRepository;
	private final FileStorageService fileStorageService;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Transactional
	public ImageCreateResponse createImage(ImageCreateRequest imageCreateRequest) {
		if (isNotImage(imageCreateRequest.multipartFile())) {
			throw new IllegalArgumentException(BAD_IMAGE_REQUEST.getMessage());
		}

		String imageUrl = fileStorageService.upload(
				imageCreateRequest.multipartFile(), imageCreateRequest.imageType().getSubPath()
		);

		Image image = ImageMapper.toEntity(imageCreateRequest, imageUrl);
		applicationEventPublisher.publishEvent(new RollbackUploadEvent(image));
		Image savedImage = imageRepository.save(image);
		return new ImageCreateResponse(savedImage.getId(), savedImage.getImageUrl());
	}

	@Transactional
	public void deleteImage(Long imageId) {
		Image image = imageRepository.findById(imageId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));

		deleteImage(image);
	}

	@Transactional
	public void deleteImages(ImageDeleteRequest imageDeleteRequest) {
		List<Image> images = imageRepository.findAllByImageTypeAndReferenceIdAndDeletedAtIsNull(
				imageDeleteRequest.imageType(), imageDeleteRequest.referenceId()
		);

		images.forEach(this::deleteImage);
	}

	private void deleteImage(Image image) {
		image.delete();
		applicationEventPublisher.publishEvent(new ImageDeleteEvent(image));
	}

	private boolean isNotImage(MultipartFile multipartFile) {
		return !Objects.equals(multipartFile.getContentType(), MediaType.IMAGE_GIF_VALUE)
				&& !Objects.equals(multipartFile.getContentType(), MediaType.IMAGE_JPEG_VALUE)
				&& !Objects.equals(multipartFile.getContentType(), MediaType.IMAGE_PNG_VALUE);
	}
}