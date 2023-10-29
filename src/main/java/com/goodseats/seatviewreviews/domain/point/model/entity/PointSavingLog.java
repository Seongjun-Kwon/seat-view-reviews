package com.goodseats.seatviewreviews.domain.point.model.entity;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.goodseats.seatviewreviews.domain.BaseEntity;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.point.model.vo.PointStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_saving_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointSavingLog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "detail", length = 20, nullable = false)
	private String detail;

	@Column(name = "saving_amount", nullable = false)
	private int savingAmount;

	@Column(name = "rest_amount", nullable = false)
	private int restAmount;

	@Column(name = "expiration_date", nullable = false)
	private LocalDate expirationDate;

	@Column(name = "status", length = 15, nullable = false)
	@Enumerated(value = EnumType.STRING)
	private PointStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	private PointSavingLog(String detail, int savingAmount, Member member) {
		this.detail = detail;
		this.savingAmount = savingAmount;
		this.restAmount = savingAmount;
		this.expirationDate = LocalDate.now().plusYears(1);
		this.status = PointStatus.AVAILABLE;
		this.member = member;
	}

	public void minus(int usingAmount) {
		if (usingAmount >= this.restAmount) {
			minusAll();
		} else {
			this.restAmount -= usingAmount;
		}

	}

	public void setMember(Member member) {
		if (Objects.nonNull(this.member)) {
			this.member.getPointSavingLogs().remove(this);
		}

		this.member = member;
		member.getPointSavingLogs().add(this);
	}

	private void minusAll() {
		this.restAmount = 0;
		this.status = PointStatus.USED;
	}
}
