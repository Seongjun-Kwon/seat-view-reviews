package com.goodseats.seatviewreviews.domain.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.image.model.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
