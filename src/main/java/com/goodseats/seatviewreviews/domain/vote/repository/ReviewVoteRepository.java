package com.goodseats.seatviewreviews.domain.vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.vote.model.entity.ReviewVote;
import com.goodseats.seatviewreviews.domain.vote.model.vo.VoteChoice;

public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {

	boolean existsByMemberAndReview(Member member, Review review);

	int countReviewVoteByReviewAndVoteChoice(Review review, VoteChoice voteChoice);
}
