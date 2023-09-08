package com.goodseats.seatviewreviews.domain.review.mapper;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewVotesResponse;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.model.entity.ReviewVote;
import com.goodseats.seatviewreviews.domain.review.model.vo.VoteChoice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewVoteMapper {

	public static ReviewVote toEntity(Member member, Review review, VoteChoice voteChoice) {
		return new ReviewVote(voteChoice, member, review);
	}

	public static ReviewVotesResponse toClickedReviewVotesResponse(Review review, ReviewVote reviewVote) {
		return new ReviewVotesResponse(
				review.getLikeCount(), review.getDislikeCount(), reviewVote.isLike(), reviewVote.isDislike()
		);
	}

	public static ReviewVotesResponse toNotClickedReviewVotesResponse(Review review) {
		return new ReviewVotesResponse(
				review.getLikeCount(), review.getDislikeCount(), false, false
		);
	}
}
