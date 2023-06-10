package com.goodseats.seatviewreviews.domain.review.controller;

import static com.goodseats.seatviewreviews.common.constant.CookieConstant.*;
import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;

import java.net.URI;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.goodseats.seatviewreviews.common.security.Authority;
import com.goodseats.seatviewreviews.common.security.SessionConstant;
import com.goodseats.seatviewreviews.common.util.CookieUtils;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewPublishRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.TempReviewCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewDetailResponse;
import com.goodseats.seatviewreviews.domain.review.service.ReviewRedisFacade;
import com.goodseats.seatviewreviews.domain.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

	private final ReviewService reviewService;
	private final ReviewRedisFacade reviewRedisFacade;

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

	@Authority(authorities = {USER, ADMIN})
	@PatchMapping(value = "/{reviewId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<Void> publishReview(
			@Valid @ModelAttribute ReviewPublishRequest reviewPublishRequest,
			@PathVariable Long reviewId,
			@SessionAttribute(value = SessionConstant.LOGIN_MEMBER_INFO) AuthenticationDTO authenticationDTO
	) {
		reviewService.publishReview(reviewPublishRequest, reviewId, authenticationDTO.memberId());
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = "/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReviewDetailResponse> getReview(
			@PathVariable Long reviewId,
			@CookieValue(value = USER_KEY, required = false) Cookie userKey,
			HttpServletResponse response
	) {
		userKey = CookieUtils.setUserKey(userKey, response);
		ReviewDetailResponse reviewDetailResponse = reviewRedisFacade.getReview(userKey.getValue(), reviewId);
		return ResponseEntity.ok(reviewDetailResponse);
	}
}