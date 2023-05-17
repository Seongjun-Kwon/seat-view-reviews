package com.goodseats.seatviewreviews.domain.image.model.vo;

import lombok.Getter;

@Getter
public enum ImageType {
	MEMBER_PROFILE("members"),
	REVIEW("reviews"),
	STADIUM("stadiums");

	private final String subPath;

	ImageType(String subPath) {
		this.subPath = subPath;
	}
}
