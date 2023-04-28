package com.goodseats.seatviewreviews.domain.member.model.dto;

import javax.validation.constraints.NotBlank;

public record MemberLoginRequest(
		@NotBlank String loginEmail,
		@NotBlank String password
) {
}
