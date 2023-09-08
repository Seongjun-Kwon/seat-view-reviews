package com.goodseats.seatviewreviews.domain.review.model.dto.request;

import java.util.Optional;

import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;

public record VotesGetRequest(Long reviewId, Optional<AuthenticationDTO> authenticationDTO) {
}
