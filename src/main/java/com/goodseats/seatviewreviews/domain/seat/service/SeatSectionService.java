package com.goodseats.seatviewreviews.domain.seat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.domain.seat.mapper.SeatSectionMapper;
import com.goodseats.seatviewreviews.domain.seat.model.dto.SeatSectionsResponse;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatSectionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatSectionService {

	private final SeatSectionRepository seatSectionRepository;

	@Transactional(readOnly = true)
	public SeatSectionsResponse getSeatSections(Long stadiumId) {
		List<SeatSection> seatSections = seatSectionRepository.findAllByStadium(stadiumId);
		return SeatSectionMapper.toSeatSectionsResponse(seatSections);
	}
}
