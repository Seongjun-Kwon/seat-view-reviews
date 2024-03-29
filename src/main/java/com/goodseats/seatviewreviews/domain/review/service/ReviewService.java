package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.mapper.ReviewMapper;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewPublishRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.TempReviewCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewDetailResponse;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewsResponse;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.stadium.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final MemberRepository memberRepository;
	private final SeatRepository seatRepository;

	@Transactional
	public Long createTempReview(TempReviewCreateRequest tempReviewCreateRequest, Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
		Seat seat = seatRepository.findById(tempReviewCreateRequest.seatId())
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));

		Review review = ReviewMapper.toEntity(member, seat);
		Review savedReview = reviewRepository.save(review);
		return savedReview.getId();
	}

	@Transactional
	public void publishReview(ReviewPublishRequest reviewPublishRequest, Long reviewId, Long memberId) {
		Review tempReview = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
		tempReview.verifyWriter(memberId);

		tempReview.publish(reviewPublishRequest.title(), reviewPublishRequest.content(), reviewPublishRequest.score());
	}

	@Transactional(readOnly = true)
	public ReviewDetailResponse getReview(Long reviewId) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));

		return ReviewMapper.toReviewDetailResponse(review);
	}

	@Transactional(readOnly = true)
	public ReviewsResponse getReviews(Long seatId, Pageable pageable) {
		Seat seat = seatRepository.findById(seatId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));

		Page<Review> reviewPage = reviewRepository.findAllWithFetchMemberBySeatIdAndPublishedTrue(seat.getId(), pageable);
		return ReviewMapper.toReviewsResponse(reviewPage);
	}
}