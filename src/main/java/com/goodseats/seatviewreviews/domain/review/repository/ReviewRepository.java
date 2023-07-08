package com.goodseats.seatviewreviews.domain.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findAllBySeatAndPublishedTrue(Seat seat, Pageable pageable);
}
