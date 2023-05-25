package com.goodseats.seatviewreviews.domain.image.event;

import com.goodseats.seatviewreviews.domain.image.model.entity.Image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ImageDeleteEvent {

	private final Image image;
}
