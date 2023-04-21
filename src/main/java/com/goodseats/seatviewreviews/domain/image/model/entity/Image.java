package com.goodseats.seatviewreviews.domain.image.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.goodseats.seatviewreviews.domain.BaseEntity;
import com.goodseats.seatviewreviews.domain.image.model.vo.ImageType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Image extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private ImageType imageType;

	private Long referenceId;

	private String imageUrl;

	private String uploadedName;

	private String savedName;

	public Image(ImageType imageType, Long referenceId, String imageUrl, String uploadedName, String savedName) {
		this.imageType = imageType;
		this.referenceId = referenceId;
		this.imageUrl = imageUrl;
		this.uploadedName = uploadedName;
		this.savedName = savedName;
	}
}
