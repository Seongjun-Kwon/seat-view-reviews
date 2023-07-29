package com.goodseats.seatviewreviews.domain.image.model.dto.request;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.goodseats.seatviewreviews.domain.image.model.vo.ImageType;

public record ImageCreateRequest(
		@NotNull MultipartFile multipartFile,
		@NotNull ImageType imageType,
		@NotNull Long referenceId
) {
}