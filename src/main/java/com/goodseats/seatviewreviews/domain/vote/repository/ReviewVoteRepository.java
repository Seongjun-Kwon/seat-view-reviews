package com.goodseats.seatviewreviews.domain.vote.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.vote.model.entity.ReviewVote;
import com.goodseats.seatviewreviews.domain.vote.model.vo.VoteChoice;

public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {

	boolean existsByMemberAndReview(Member member, Review review);

	Optional<ReviewVote> findReviewVoteByMemberAndReview(Member member, Review review);

	@Query("SELECT COUNT(*) FROM ReviewVote rv WHERE rv.review.id = :reviewId AND rv.voteChoice = :voteChoice")
	int getVoteCount(@Param("reviewId") Long reviewId, @Param("voteChoice") VoteChoice voteChoice);
}