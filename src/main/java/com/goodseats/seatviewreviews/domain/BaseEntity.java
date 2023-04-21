package com.goodseats.seatviewreviews.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

	@Column(name = "created_at", nullable = false)
	@CreatedDate
	private LocalDateTime createdAt;

	@Column(name = "last_updated_at", nullable = false)
	@LastModifiedDate
	private LocalDateTime lastUpdatedAt;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;
}
