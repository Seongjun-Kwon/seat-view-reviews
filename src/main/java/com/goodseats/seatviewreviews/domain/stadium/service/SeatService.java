package com.goodseats.seatviewreviews.domain.stadium.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.domain.stadium.mapper.SeatMapper;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.SeatsResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.stadium.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatService {

	private final SeatRepository seatRepository;

	@Transactional(readOnly = true)
	public SeatsResponse getSeats(Long sectionId) {
		List<Seat> seats = seatRepository.findAllBySeatSection(sectionId);
		return SeatMapper.toSeatsResponse(seats);
	}
}
