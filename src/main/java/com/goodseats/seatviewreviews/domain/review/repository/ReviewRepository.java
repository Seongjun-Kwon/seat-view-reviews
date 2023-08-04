package com.goodseats.seatviewreviews.domain.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.vote.repository.VoteTypeRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, VoteTypeRepository {

	@Override
	@Query(value = "SELECT COUNT(r) > 0 FROM Review r WHERE r.id = :referenceId")
	boolean existsByReferenceId(@Param(value = "referenceId") Long referenceId);

	@Query(value = "SELECT r FROM Review r JOIN FETCH r.member WHERE r.seat.id = :seatId AND r.published IS TRUE",
			countQuery = "SELECT count(r) FROM Review r WHERE r.seat.id = :seatId AND r.published IS TRUE")
	Page<Review> findAllWithFetchMemberBySeatIdAndPublishedTrue(@Param("seatId") Long seatId, Pageable pageable);
}