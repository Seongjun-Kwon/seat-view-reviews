package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.mapper.ReviewMapper;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewCreateRequest;
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
	public Long createReview(ReviewCreateRequest reviewCreateRequest, Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
		Seat seat = seatRepository.findById(reviewCreateRequest.seatId())
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));

		Review review = ReviewMapper.toEntity(reviewCreateRequest, member, seat);
		Review savedReview = reviewRepository.save(review);

		return savedReview.getId();
	}
}