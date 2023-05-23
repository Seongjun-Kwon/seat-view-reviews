package com.goodseats.seatviewreviews.domain.image.controller;

import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;
import static org.springframework.http.MediaType.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goodseats.seatviewreviews.common.security.Authority;
import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageCreateRequest;
import com.goodseats.seatviewreviews.domain.image.model.dto.request.ImageDeleteRequest;
import com.goodseats.seatviewreviews.domain.image.model.dto.response.ImageCreateResponse;
import com.goodseats.seatviewreviews.domain.image.service.ImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageController {

	private final ImageService imageService;

	@Authority(authorities = {USER, ADMIN})
	@PostMapping(consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ImageCreateResponse> createImage(@ModelAttribute ImageCreateRequest imageCreateRequest) {
		ImageCreateResponse imageCreateResponse = imageService.createImage(imageCreateRequest);
		return ResponseEntity.ok(imageCreateResponse);
	}

	@Authority(authorities = {USER, ADMIN})
	@DeleteMapping("/{imageId}")
	public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
		imageService.deleteImage(imageId);
		return ResponseEntity.noContent().build();
	}

	@Authority(authorities = {USER, ADMIN})
	@DeleteMapping
	public ResponseEntity<Void> deleteImages(@RequestBody ImageDeleteRequest imageDeleteRequest) {
		imageService.deleteImages(imageDeleteRequest);
		return ResponseEntity.noContent().build();
	}
}