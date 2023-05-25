package com.goodseats.seatviewreviews.domain.image.model.dto.request;

import com.goodseats.seatviewreviews.domain.image.model.vo.ImageType;

public record ImageDeleteRequest(ImageType imageType, Long referenceId) {
}
