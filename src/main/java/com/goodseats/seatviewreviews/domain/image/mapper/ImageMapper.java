package com.goodseats.seatviewreviews.domain.image.mapper;

import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageCreateRequest;
import com.goodseats.seatviewreviews.domain.image.model.entity.Image;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageMapper {

	public static Image toEntity(ImageCreateRequest imageCreateRequest, String imageUrl) {
		return new Image(imageCreateRequest.imageType(),
				imageCreateRequest.referenceId(),
				imageUrl,
				imageCreateRequest.multipartFile().getOriginalFilename()
		);
	}
}
