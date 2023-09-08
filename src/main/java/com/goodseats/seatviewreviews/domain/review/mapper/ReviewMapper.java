package com.goodseats.seatviewreviews.domain.review.mapper;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewDetailResponse;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewsElementResponse;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewsResponse;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Seat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {

	public static Review toEntity(Member member, Seat seat) {
		return new Review(member, seat);
	}

	public static ReviewDetailResponse toReviewDetailResponse(Review review) {
		return new ReviewDetailResponse(
				review.getTitle(), review.getContent(), review.getScore(), review.getViewCount(),
				review.getMember().getNickname()
		);
	}

	public static ReviewsElementResponse toReviewsElementResponse(Review review) {
		return new ReviewsElementResponse(
				review.getId(), review.getTitle(), review.getScore(), review.getViewCount(), review.getMember().getNickname()
		);
	}

	public static ReviewsResponse toReviewsResponse(Page<Review> reviewPage) {
		return new ReviewsResponse(
				reviewPage.map(ReviewMapper::toReviewsElementResponse)
						.stream()
						.collect(Collectors.toList())
		);
	}
}