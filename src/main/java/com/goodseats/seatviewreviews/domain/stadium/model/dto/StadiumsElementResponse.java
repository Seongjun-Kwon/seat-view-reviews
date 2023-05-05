package com.goodseats.seatviewreviews.domain.stadium.model.dto;

import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;

public record StadiumsElementResponse(Long stadiumId, String name, HomeTeam homeTeam) {
}
