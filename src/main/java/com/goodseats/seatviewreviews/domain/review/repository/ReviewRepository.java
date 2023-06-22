package com.goodseats.seatviewreviews.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goodseats.seatviewreviews.domain.review.model.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("UPDATE Review r SET r.viewCount = :viewCount WHERE r.id=:reviewId")
	void updateViewCount(@Param("viewCount") int viewCount, @Param("reviewId") Long reviewId);
}
