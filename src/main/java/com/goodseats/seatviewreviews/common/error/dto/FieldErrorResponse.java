package com.goodseats.seatviewreviews.common.error.dto;

public record FieldErrorResponse(String fieldName, Object rejectedValue, String message) {
}
