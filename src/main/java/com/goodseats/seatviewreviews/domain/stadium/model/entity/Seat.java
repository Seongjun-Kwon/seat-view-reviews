package com.goodseats.seatviewreviews.domain.stadium.model.entity;

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
@Table(name = "seat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Seat extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "seat_info", length = 10, nullable = false)
	private String seatInfo;

	@Column(name = "average_score", nullable = false)
	private float averageScore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_grade_id")
	private SeatGrade seatGrade;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_section_id")
	private SeatSection seatSection;

	public Seat(String seatInfo, SeatGrade seatGrade, SeatSection seatSection) {
		this.seatInfo = seatInfo;
		this.averageScore = 0;
		this.seatGrade = seatGrade;
		this.seatSection = seatSection;
	}
}
