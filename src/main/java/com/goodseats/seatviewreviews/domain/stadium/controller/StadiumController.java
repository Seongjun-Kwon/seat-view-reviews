package com.goodseats.seatviewreviews.domain.stadium.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.goodseats.seatviewreviews.domain.stadium.model.dto.StadiumsResponse;
import com.goodseats.seatviewreviews.domain.stadium.service.StadiumService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/stadiums")
public class StadiumController {

	private final StadiumService stadiumService;

	@GetMapping
	public ResponseEntity<StadiumsResponse> getStadiums() {
		StadiumsResponse stadiumsResponse = stadiumService.getStadiums();
		return ResponseEntity.ok(stadiumsResponse);
	}
}
