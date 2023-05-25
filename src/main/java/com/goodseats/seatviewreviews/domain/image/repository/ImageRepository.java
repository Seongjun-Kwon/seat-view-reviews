package com.goodseats.seatviewreviews.domain.image.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.image.model.entity.Image;
import com.goodseats.seatviewreviews.domain.image.model.vo.ImageType;

public interface ImageRepository extends JpaRepository<Image, Long> {

	List<Image> findAllByImageTypeAndReferenceIdAndDeletedAtIsNull(ImageType imageType, Long referenceId);
}
