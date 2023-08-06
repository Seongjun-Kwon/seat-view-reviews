package com.goodseats.seatviewreviews.domain.vote.model.dto.request;

import javax.validation.constraints.NotNull;

import com.goodseats.seatviewreviews.domain.vote.model.vo.VoteChoice;

public record ReviewVoteCreateRequest(
		@NotNull Long reviewId,
		@NotNull VoteChoice voteChoice
) {
}
