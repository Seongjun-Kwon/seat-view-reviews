package com.goodseats.seatviewreviews.domain.stadium.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.common.error.exception.ErrorCode;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.stadium.mapper.StadiumMapper;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumDetailResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumsElementResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumsResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.repository.StadiumRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StadiumService {

	private final StadiumRepository stadiumRepository;

	@Transactional(readOnly = true)
	public StadiumsResponse getStadiums() {
		List<StadiumsElementResponse> elementResponses = stadiumRepository.findAll()
				.stream()
				.map(StadiumMapper::toStadiumsElementResponse)
				.toList();

		return new StadiumsResponse(elementResponses);
	}

	@Transactional(readOnly = true)
	public StadiumDetailResponse getStadium(Long stadiumId) {
		Stadium stadium = stadiumRepository.findById(stadiumId)
				.orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

		return StadiumMapper.toStadiumDetailResponse(stadium);
	}
}
