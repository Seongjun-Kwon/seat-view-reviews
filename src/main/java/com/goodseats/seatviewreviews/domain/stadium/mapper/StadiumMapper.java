package com.goodseats.seatviewreviews.domain.stadium.mapper;

import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumDetailResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumsElementResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StadiumMapper {

	public static StadiumsElementResponse toStadiumsElementResponse(Stadium stadium) {
		return new StadiumsElementResponse(stadium.getId(), stadium.getName(), stadium.getHomeTeam());
	}

	public static StadiumDetailResponse toStadiumDetailResponse(Stadium stadium) {
		return new StadiumDetailResponse(stadium.getName(), stadium.getAddress());
	}
}
