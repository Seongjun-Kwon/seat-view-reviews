package com.goodseats.seatviewreviews.domain.point.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.goodseats.seatviewreviews.domain.BaseEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_using_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointUsingDetail extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "using_amount", nullable = false)
	private int usingAmount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "point_saving_log_id")
	private PointSavingLog pointSavingLog;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "point_using_log_id")
	private PointUsingLog pointUsingLog;

	public PointUsingDetail(int usingAmount, PointSavingLog pointSavingLog, PointUsingLog pointUsingLog) {
		this.usingAmount = usingAmount;
		this.pointSavingLog = pointSavingLog;
		this.pointUsingLog = pointUsingLog;
	}
}