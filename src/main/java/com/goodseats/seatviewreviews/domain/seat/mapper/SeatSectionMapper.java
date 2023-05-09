package com.goodseats.seatviewreviews.domain.seat.mapper;

import java.util.List;

import com.goodseats.seatviewreviews.domain.seat.model.dto.SeatSectionsElementResponse;
import com.goodseats.seatviewreviews.domain.seat.model.dto.SeatSectionsResponse;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SeatSectionMapper {

	public static SeatSectionsResponse toSeatSectionsResponse(List<SeatSection> seatSections) {
		List<SeatSectionsElementResponse> elementResponses = seatSections.stream()
				.map(seatSection -> new SeatSectionsElementResponse(seatSection.getId(), seatSection.getName()))
				.toList();

		return new SeatSectionsResponse(elementResponses);
	}
}
