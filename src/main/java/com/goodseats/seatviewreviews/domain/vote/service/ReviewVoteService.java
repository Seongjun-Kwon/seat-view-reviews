package com.goodseats.seatviewreviews.domain.vote.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import java.util.Optional;

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
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.ReviewVotesGetRequest;
import com.goodseats.seatviewreviews.domain.vote.model.dto.response.ReviewVotesResponse;
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
		Member member = getMemberById(memberId);
		Review review = getReviewById(reviewVoteCreateRequest.reviewId());

		if (reviewVoteRepository.existsByMemberAndReview(member, review)) {
			throw new DuplicatedException(ALREADY_VOTED);
		}

		ReviewVote reviewVote = ReviewVoteMapper.toEntity(member, review, reviewVoteCreateRequest.voteChoice());
		reviewVoteRepository.save(reviewVote);

		return reviewVote.getId();
	}

	@Transactional
	public void deleteVote(Long reviewVoteId, Long memberId) {
		ReviewVote reviewVote = getReviewVoteById(reviewVoteId);
		reviewVote.verifyVoter(memberId);

		reviewVoteRepository.delete(reviewVote);
	}

	@Transactional(readOnly = true)
	public ReviewVotesResponse getVotes(ReviewVotesGetRequest reviewVotesGetRequest) {
		Review review = getReviewById(reviewVotesGetRequest.reviewId());

		return reviewVotesGetRequest.authenticationDTO()
				.map(authenticationDTO -> getMemberById(authenticationDTO.memberId()))
				.map(member -> {
					Optional<ReviewVote> optionalReviewVote
							= reviewVoteRepository.findReviewVoteByMemberAndReview(member, review);

					return optionalReviewVote
							.map(reviewVote -> ReviewVoteMapper.toClickedReviewVotesResponse(review, reviewVote))
							.orElseGet(() -> ReviewVoteMapper.toNotClickedReviewVotesResponse(review));
				})
				.orElseGet(() -> ReviewVoteMapper.toNotClickedReviewVotesResponse(review));
	}

	private Member getMemberById(Long memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
	}

	private Review getReviewById(Long reviewId) {
		return reviewRepository.findById(reviewId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
	}

	private ReviewVote getReviewVoteById(Long reviewVoteId) {
		return reviewVoteRepository.findById(reviewVoteId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
	}
}