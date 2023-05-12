package com.goodseats.seatviewreviews.domain.review.controller;

import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.goodseats.seatviewreviews.common.security.Authority;
import com.goodseats.seatviewreviews.common.security.SessionConstant;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewCreateRequest;
import com.goodseats.seatviewreviews.domain.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	@Authority(authorities = {USER, ADMIN})
	@PostMapping
	public ResponseEntity<Void> createReview(
			@Valid @ModelAttribute ReviewCreateRequest reviewCreateRequest,
			@SessionAttribute(value = SessionConstant.LOGIN_MEMBER_INFO, required = false) AuthenticationDTO authenticationDTO
	) {

		Long reviewId = reviewService.createReview(reviewCreateRequest, authenticationDTO.memberId());
		return ResponseEntity.created(URI.create("/api/v1/reviews/" + reviewId)).build();
	}
}
