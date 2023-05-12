package com.goodseats.seatviewreviews.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.review.model.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
