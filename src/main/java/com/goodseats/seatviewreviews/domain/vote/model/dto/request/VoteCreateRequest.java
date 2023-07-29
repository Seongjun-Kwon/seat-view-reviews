package com.goodseats.seatviewreviews.domain.vote.model.dto.request;

import javax.validation.constraints.NotNull;

import com.goodseats.seatviewreviews.domain.vote.model.vo.VoteChoice;
import com.goodseats.seatviewreviews.domain.vote.model.vo.VoteType;

public record VoteCreateRequest(
		@NotNull Long memberId,
		@NotNull VoteType voteType,
		@NotNull Long referenceId,
		@NotNull VoteChoice voteChoice
) {
}
