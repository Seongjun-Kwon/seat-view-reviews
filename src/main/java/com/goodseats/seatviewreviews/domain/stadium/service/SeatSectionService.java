package com.goodseats.seatviewreviews.domain.stadium.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.domain.stadium.mapper.SeatSectionMapper;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.SeatSectionsResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.stadium.repository.SeatSectionRepository;

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
