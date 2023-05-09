package com.goodseats.seatviewreviews.domain.seat.mapper;

import java.util.List;

import com.goodseats.seatviewreviews.domain.seat.model.dto.response.SeatsElementResponse;
import com.goodseats.seatviewreviews.domain.seat.model.dto.response.SeatsResponse;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SeatMapper {

	public static SeatsResponse toSeatsResponse(List<Seat> seats) {
		List<SeatsElementResponse> elementResponses = seats.stream()
				.map(seat -> new SeatsElementResponse(seat.getSeatInfo(), seat.getAverageScore()))
				.toList();

		return new SeatsResponse(elementResponses);
	}
}
