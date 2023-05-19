package com.goodseats.seatviewreviews.domain.review.model.dto.request;

import javax.validation.constraints.NotNull;

public record TempReviewCreateRequest(@NotNull Long seatId) {
}
