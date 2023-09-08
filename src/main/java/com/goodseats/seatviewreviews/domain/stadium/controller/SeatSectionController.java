package com.goodseats.seatviewreviews.domain.stadium.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.SeatSectionsResponse;
import com.goodseats.seatviewreviews.domain.stadium.service.SeatSectionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sections")
public class SeatSectionController {

	private final SeatSectionService seatSectionService;

	@GetMapping
	public ResponseEntity<SeatSectionsResponse> getSeatSections(@RequestParam Long stadiumId) {
		SeatSectionsResponse seatSectionsResponse = seatSectionService.getSeatSections(stadiumId);
		return ResponseEntity.ok(seatSectionsResponse);
	}
}
