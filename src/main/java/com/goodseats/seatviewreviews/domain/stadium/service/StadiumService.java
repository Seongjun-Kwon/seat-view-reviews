package com.goodseats.seatviewreviews.domain.stadium.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.domain.stadium.mapper.StadiumMapper;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.StadiumsElementResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.StadiumsResponse;
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
}
