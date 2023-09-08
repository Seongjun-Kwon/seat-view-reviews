package com.goodseats.seatviewreviews.domain.stadium.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.stadium.model.entity.SeatGrade;

public interface SeatGradeRepository extends JpaRepository<SeatGrade, Long> {
}
