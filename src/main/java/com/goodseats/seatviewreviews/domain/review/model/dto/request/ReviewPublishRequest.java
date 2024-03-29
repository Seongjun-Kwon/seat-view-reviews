package com.goodseats.seatviewreviews.domain.review.model.dto.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

public record ReviewPublishRequest(
		@NotBlank @Length(max = 50) String title,
		@NotBlank @Length(max = 10000) String content,
		@Min(0) @Max(5) int score
) {
}
