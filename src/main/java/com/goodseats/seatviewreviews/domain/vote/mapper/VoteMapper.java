package com.goodseats.seatviewreviews.domain.vote.mapper;

import static com.goodseats.seatviewreviews.domain.vote.model.vo.VoteType.*;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.VoteCreateRequest;
import com.goodseats.seatviewreviews.domain.vote.model.entity.Vote;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VoteMapper {

	public static Vote toEntity(VoteCreateRequest voteCreateRequest, Member member) {
		return
				switch (voteCreateRequest.voteType()) {
					case REVIEW -> new Vote(
							REVIEW, voteCreateRequest.referenceId(), voteCreateRequest.voteChoice(), member
					);
					case COMMENT -> new Vote(
							COMMENT, voteCreateRequest.referenceId(), voteCreateRequest.voteChoice(), member
					);
				};
	}
}
