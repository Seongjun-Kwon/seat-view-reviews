package com.goodseats.seatviewreviews.domain.image.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

	@Column(name = "image_type", length = 30, nullable = false)
	@Enumerated(EnumType.STRING)
	private ImageType imageType;

	@Column(name = "reference_id", nullable = false)
	private Long referenceId;

	@Column(name = "image_url", length = 500, nullable = false)
	private String imageUrl;

	@Column(name = "uploaded_name", length = 50, nullable = false)
	private String uploadedName;

	@Column(name = "saved_name", length = 50, nullable = false)
	private String savedName;

	public Image(ImageType imageType, Long referenceId, String imageUrl, String uploadedName) {
		this.imageType = imageType;
		this.referenceId = referenceId;
		this.imageUrl = imageUrl;
		this.uploadedName = uploadedName;
		this.savedName = extractSavedName(imageUrl);
	}

	private String extractSavedName(String imageUrl) {
		int beforeSavedNameIndex = imageUrl.lastIndexOf("/");
		return imageUrl.substring(beforeSavedNameIndex + 1);
	}
}
