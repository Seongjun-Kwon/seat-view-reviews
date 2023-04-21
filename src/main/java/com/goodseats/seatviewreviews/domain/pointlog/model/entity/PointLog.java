package com.goodseats.seatviewreviews.domain.pointlog.model.entity;

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
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointLog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "used_amount", nullable = false)
	private int usedAmount;

	@Column(name = "used_field", length = 30, nullable = false)
	private String usedField;

	@Column(name = "remaining_point", nullable = false)
	private int remainingPoint;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public PointLog(int usedAmount, String usedField, int remainingPoint, Member member) {
		this.usedAmount = usedAmount;
		this.usedField = usedField;
		this.remainingPoint = remainingPoint;
		this.member = member;
	}
}
