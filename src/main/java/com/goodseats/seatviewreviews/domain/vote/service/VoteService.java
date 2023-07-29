package com.goodseats.seatviewreviews.domain.vote.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.vote.mapper.VoteMapper;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.VoteCreateRequest;
import com.goodseats.seatviewreviews.domain.vote.model.entity.Vote;
import com.goodseats.seatviewreviews.domain.vote.repository.VoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteService {

	private final VoteRepository voteRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void createUpVote(VoteCreateRequest voteCreateRequest) {
		Member member = memberRepository.findById(voteCreateRequest.memberId())
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));

		if (voteRepository.existsByMemberAndVoteTypeAndReferenceId(
				member, voteCreateRequest.voteType(), voteCreateRequest.referenceId()
		)) {
			throw new DuplicatedException(ALREADY_VOTED);
		}

		Vote vote = VoteMapper.toEntity(voteCreateRequest, member);

		voteRepository.save(vote);
	}
}
