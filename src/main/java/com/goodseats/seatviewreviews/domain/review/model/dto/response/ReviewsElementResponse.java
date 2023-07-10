package com.goodseats.seatviewreviews.domain.review.model.dto.response;

public record ReviewsElementResponse(Long reviewId, String title, int score, int viewCount, String writer) {
}
