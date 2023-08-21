package com.goodseats.seatviewreviews.domain.vote.model.dto.response;

public record ReviewVotesResponse(
		int likeCount,
		int dislikeCount,
		boolean clickLike,
		boolean clickDislike
) {
}
