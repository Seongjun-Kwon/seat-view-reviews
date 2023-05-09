package com.goodseats.seatviewreviews.domain.member.model.dto.request;

import javax.validation.constraints.NotBlank;

public record MemberLoginRequest(
		@NotBlank String loginEmail,
		@NotBlank String password
) {
}
