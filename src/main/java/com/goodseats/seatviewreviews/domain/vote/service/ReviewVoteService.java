package com.goodseats.seatviewreviews.domain.vote.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.vote.mapper.ReviewVoteMapper;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.ReviewVoteCreateRequest;
import com.goodseats.seatviewreviews.domain.vote.model.entity.ReviewVote;
import com.goodseats.seatviewreviews.domain.vote.repository.ReviewVoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewVoteService {

	private final ReviewVoteRepository reviewVoteRepository;
	private final MemberRepository memberRepository;
	private final ReviewRepository reviewRepository;

	@Transactional
	public Long createVote(ReviewVoteCreateRequest reviewVoteCreateRequest, Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
		Review review = reviewRepository.findById(reviewVoteCreateRequest.reviewId())
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));

		if (reviewVoteRepository.existsByMemberAndReview(member, review)) {
			throw new DuplicatedException(ALREADY_VOTED);
		}

		ReviewVote reviewVote = ReviewVoteMapper.toEntity(member, review, reviewVoteCreateRequest.voteChoice());
		reviewVoteRepository.save(reviewVote);

		return review.getId();
	}

	@Transactional
	public void deleteVote(Long voteId, Long memberId) {
		ReviewVote reviewVote = reviewVoteRepository.findById(voteId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
		reviewVote.verifyVoter(memberId);

		reviewVoteRepository.delete(reviewVote);
	}
}
