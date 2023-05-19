package com.goodseats.seatviewreviews.domain.review.controller;

import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.goodseats.seatviewreviews.common.security.Authority;
import com.goodseats.seatviewreviews.common.security.SessionConstant;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.TempReviewCreateRequest;
import com.goodseats.seatviewreviews.domain.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	@Authority(authorities = {USER, ADMIN})
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> createTempReview(
			@Valid @RequestBody TempReviewCreateRequest tempReviewCreateRequest,
			@SessionAttribute(value = SessionConstant.LOGIN_MEMBER_INFO) AuthenticationDTO authenticationDTO,
			HttpServletRequest request
	) {
		Long tempReviewId = reviewService.createTempReview(tempReviewCreateRequest, authenticationDTO.memberId());
		return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + tempReviewId)).build();
	}
}
