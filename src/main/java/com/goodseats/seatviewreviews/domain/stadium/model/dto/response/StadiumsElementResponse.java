package com.goodseats.seatviewreviews.domain.stadium.model.dto.response;

import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;

public record StadiumsElementResponse(Long stadiumId, String name, HomeTeam homeTeam) {
}
