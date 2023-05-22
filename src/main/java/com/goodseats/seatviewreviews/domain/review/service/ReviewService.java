package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.mapper.ReviewMapper;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewPublishRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.TempReviewCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatRepository;

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
}