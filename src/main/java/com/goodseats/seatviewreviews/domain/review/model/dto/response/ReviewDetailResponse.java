package com.goodseats.seatviewreviews.domain.review.model.dto.response;

public record ReviewDetailResponse(String title, String content, int score, int hits, String writer) {
}
