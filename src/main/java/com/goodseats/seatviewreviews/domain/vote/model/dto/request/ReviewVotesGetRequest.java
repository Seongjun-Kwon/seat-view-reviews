package com.goodseats.seatviewreviews.domain.vote.model.dto.request;

import java.util.Optional;

import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;

public record ReviewVotesGetRequest(Long reviewId, Optional<AuthenticationDTO> authenticationDTO) {
}
