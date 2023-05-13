package com.goodseats.seatviewreviews.domain.review.mapper;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {

	public static Review toEntity(ReviewCreateRequest reviewCreateRequest, Member member, Seat seat) {
		return new Review(
				reviewCreateRequest.title(),
				reviewCreateRequest.content(),
				reviewCreateRequest.score(),
				member,
				seat
		);
	}
}
