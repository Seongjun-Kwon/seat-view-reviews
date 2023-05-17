package com.goodseats.seatviewreviews.domain.image.model.dto.request;

import org.springframework.web.multipart.MultipartFile;

import com.goodseats.seatviewreviews.domain.image.model.vo.ImageType;

public record ImageCreateRequest(MultipartFile multipartFile, ImageType imageType, Long referenceId) {
}