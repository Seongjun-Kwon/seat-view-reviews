package com.goodseats.seatviewreviews.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.common.error.exception.ErrorCode;

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

	public void delete() {
		if (Objects.nonNull(deletedAt)) {
			throw new DuplicatedException(ErrorCode.ALREADY_DELETED);
		}

		deletedAt = LocalDateTime.now();
	}
}
