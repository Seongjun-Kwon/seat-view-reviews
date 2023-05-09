package com.goodseats.seatviewreviews.domain.seat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;

public interface SeatGradeRepository extends JpaRepository<SeatGrade, Long> {
}
