package com.goodseats.seatviewreviews.domain.review.model.dto.request;

import javax.validation.constraints.NotNull;

import com.goodseats.seatviewreviews.domain.review.model.vo.VoteChoice;

public record VoteCreateRequest(
		@NotNull Long reviewId,
		@NotNull VoteChoice voteChoice
) {
}
