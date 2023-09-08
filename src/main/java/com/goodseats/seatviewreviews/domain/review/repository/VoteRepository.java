package com.goodseats.seatviewreviews.domain.review.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.model.entity.Vote;
import com.goodseats.seatviewreviews.domain.review.model.vo.VoteChoice;

public interface VoteRepository extends JpaRepository<Vote, Long> {

	boolean existsByMemberAndReview(Member member, Review review);

	Optional<Vote> findVoteByMemberAndReview(Member member, Review review);

	@Query("SELECT COUNT(*) FROM Vote v WHERE v.review.id = :reviewId AND v.voteChoice = :voteChoice")
	int getVoteCount(@Param("reviewId") Long reviewId, @Param("voteChoice") VoteChoice voteChoice);
}