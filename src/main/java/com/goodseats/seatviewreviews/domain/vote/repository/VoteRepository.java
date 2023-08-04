package com.goodseats.seatviewreviews.domain.vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.vote.model.entity.Vote;
import com.goodseats.seatviewreviews.domain.vote.model.vo.VoteType;

public interface VoteRepository extends JpaRepository<Vote, Long> {

	boolean existsByMemberAndVoteTypeAndReferenceId(Member member, VoteType voteType, Long referenceId);
}
