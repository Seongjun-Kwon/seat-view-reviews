package com.goodseats.seatviewreviews.domain.image.controller;

import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;
import static org.springframework.http.MediaType.*;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goodseats.seatviewreviews.common.security.Authority;
import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageCreateRequest;
import com.goodseats.seatviewreviews.domain.image.service.ImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageController {

	private final ImageService imageService;

	@Authority(authorities = {USER, ADMIN})
	@PostMapping(consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> createImage(@ModelAttribute ImageCreateRequest imageCreateRequest) {
		String imageUrl = imageService.createImage(imageCreateRequest);
		return ResponseEntity.created(URI.create(imageUrl)).build();
	}
}