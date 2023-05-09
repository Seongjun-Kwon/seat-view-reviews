package com.goodseats.seatviewreviews.domain.seat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;

public interface SeatSectionRepository extends JpaRepository<SeatSection, Long> {

	@Query("SELECT ss FROM SeatSection ss WHERE ss.stadium.id = :stadiumId")
	List<SeatSection> findAllByStadium(@Param("stadiumId") Long stadiumId);
}
