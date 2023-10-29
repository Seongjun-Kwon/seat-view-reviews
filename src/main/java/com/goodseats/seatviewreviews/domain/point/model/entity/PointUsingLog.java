package com.goodseats.seatviewreviews.domain.point.model.entity;

import java.util.Objects;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_using_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointUsingLog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "using_amount", nullable = false)
	private int usingAmount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	private PointUsingLog(int usingAmount, Member member) {
		this.usingAmount = usingAmount;
		this.member = member;
	}

	public void setMember(Member member) {
		if (Objects.nonNull(this.member)) {
			this.member.getPointUsingLogs().remove(this);
		}

		this.member = member;
		member.getPointUsingLogs().add(this);
	}
}
