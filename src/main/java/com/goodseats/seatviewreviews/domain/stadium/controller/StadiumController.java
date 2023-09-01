package com.goodseats.seatviewreviews.domain.stadium.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumDetailResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumsResponse;
import com.goodseats.seatviewreviews.domain.stadium.service.StadiumService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stadiums")
public class StadiumController {

	private final StadiumService stadiumService;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<StadiumsResponse> getStadiums() {
		StadiumsResponse stadiumsResponse = stadiumService.getStadiums();
		return ResponseEntity.ok(stadiumsResponse);
	}

	@GetMapping(value = "/{stadiumId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<StadiumDetailResponse> getStadium(@PathVariable Long stadiumId) {
		StadiumDetailResponse stadiumDetailResponse = stadiumService.getStadium(stadiumId);
		return ResponseEntity.ok(stadiumDetailResponse);
	}
}
