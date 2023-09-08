package com.goodseats.seatviewreviews.domain.stadium.mapper;

import java.util.List;

import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.SeatSectionsElementResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.SeatSectionsResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.SeatSection;

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
