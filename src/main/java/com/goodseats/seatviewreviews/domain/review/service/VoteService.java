package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.mapper.VoteMapper;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.VoteCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.VotesGetRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.VotesResponse;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.model.entity.Vote;
import com.goodseats.seatviewreviews.domain.review.model.vo.VoteChoice;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.review.repository.VoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteService {

	private final VoteRepository voteRepository;
	private final MemberRepository memberRepository;
	private final ReviewRepository reviewRepository;

	@Transactional
	public Long createVote(VoteCreateRequest voteCreateRequest, Long memberId) {
		Member member = getMemberById(memberId);
		Review review = getReviewById(voteCreateRequest.reviewId());

		if (voteRepository.existsByMemberAndReview(member, review)) {
			throw new DuplicatedException(ALREADY_VOTED);
		}

		Vote vote = VoteMapper.toEntity(member, review, voteCreateRequest.voteChoice());
		voteRepository.save(vote);

		syncVoteCount(review, voteCreateRequest.voteChoice());
		return vote.getId();
	}

	@Transactional
	public void deleteVote(Long voteId, Long memberId) {
		Vote vote = getVoteById(voteId);
		vote.verifyVoter(memberId);

		voteRepository.delete(vote);

		syncVoteCount(vote.getReview(), vote.getVoteChoice());
	}

	@Transactional(readOnly = true)
	public VotesResponse getVotes(VotesGetRequest votesGetRequest) {
		Review review = getReviewById(votesGetRequest.reviewId());

		return votesGetRequest.authenticationDTO()
				.map(authenticationDTO -> getMemberById(authenticationDTO.memberId()))
				.map(member -> {
					Optional<Vote> optionalVote = voteRepository.findVoteByMemberAndReview(member, review);

					return optionalVote
							.map(vote -> VoteMapper.toClickedVotesResponse(review, vote))
							.orElseGet(() -> VoteMapper.toNotClickedVotesResponse(review));
				})
				.orElseGet(() -> VoteMapper.toNotClickedVotesResponse(review));
	}

	private Member getMemberById(Long memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
	}

	private Review getReviewById(Long reviewId) {
		return reviewRepository.findById(reviewId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
	}

	private Vote getVoteById(Long voteId) {
		return voteRepository.findById(voteId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND));
	}

	private void syncVoteCount(Review review, VoteChoice voteChoice) {
		int voteCount = voteRepository.getVoteCount(review.getId(), voteChoice);
		review.updateVoteCount(voteCount, voteChoice);
	}
}