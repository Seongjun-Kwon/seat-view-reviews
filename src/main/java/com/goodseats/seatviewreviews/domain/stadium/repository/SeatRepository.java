package com.goodseats.seatviewreviews.domain.stadium.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goodseats.seatviewreviews.domain.stadium.model.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

	@Query("SELECT s FROM Seat s WHERE s.seatSection.id = :sectionId")
	List<Seat> findAllBySeatSection(@Param("sectionId") Long sectionId);
}
