package com.goodseats.seatviewreviews.domain.image.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.goodseats.seatviewreviews.common.file.FileStorageService;
import com.goodseats.seatviewreviews.domain.image.event.RollbackUploadEvent;
import com.goodseats.seatviewreviews.domain.image.mapper.ImageMapper;
import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageCreateRequest;
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
	public Long createImage(ImageCreateRequest imageCreateRequest) {
		if (isNotImage(imageCreateRequest.multipartFile())) {
			throw new IllegalArgumentException(BAD_IMAGE_REQUEST.getMessage());
		}

		String imageUrl = fileStorageService.upload(
				imageCreateRequest.multipartFile(), imageCreateRequest.imageType().getSubPath()
		);
		Image image = ImageMapper.toEntity(imageCreateRequest, imageUrl);
		applicationEventPublisher.publishEvent(new RollbackUploadEvent(image));
		imageRepository.save(image);
		return image.getId();
	}

	private boolean isNotImage(MultipartFile multipartFile) {
		return !Objects.equals(multipartFile.getContentType(), MediaType.IMAGE_GIF_VALUE)
				&& !Objects.equals(multipartFile.getContentType(), MediaType.IMAGE_JPEG_VALUE)
				&& !Objects.equals(multipartFile.getContentType(), MediaType.IMAGE_PNG_VALUE);
	}
}
