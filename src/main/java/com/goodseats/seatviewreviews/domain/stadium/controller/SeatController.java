package com.goodseats.seatviewreviews.domain.stadium.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.SeatsResponse;
import com.goodseats.seatviewreviews.domain.stadium.service.SeatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seats")
public class SeatController {

	private final SeatService seatService;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SeatsResponse> getSeats(@RequestParam Long sectionId) {
		SeatsResponse seatsResponse = seatService.getSeats(sectionId);
		return ResponseEntity.ok(seatsResponse);
	}
}
