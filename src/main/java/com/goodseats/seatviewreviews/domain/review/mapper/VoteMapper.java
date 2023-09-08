package com.goodseats.seatviewreviews.domain.review.mapper;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.VotesResponse;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.model.entity.Vote;
import com.goodseats.seatviewreviews.domain.review.model.vo.VoteChoice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VoteMapper {

	public static Vote toEntity(Member member, Review review, VoteChoice voteChoice) {
		return new Vote(voteChoice, member, review);
	}

	public static VotesResponse toClickedVotesResponse(Review review, Vote vote) {
		return new VotesResponse(review.getLikeCount(), review.getDislikeCount(), vote.isLike(), vote.isDislike());
	}

	public static VotesResponse toNotClickedVotesResponse(Review review) {
		return new VotesResponse(review.getLikeCount(), review.getDislikeCount(), false, false);
	}
}
