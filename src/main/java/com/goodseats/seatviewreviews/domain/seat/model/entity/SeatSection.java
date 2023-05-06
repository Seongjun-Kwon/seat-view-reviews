package com.goodseats.seatviewreviews.domain.seat.model.entity;

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
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seat_section")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SeatSection extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", length = 10, nullable = false)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stadium_id")
	private Stadium stadium;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_grade_id")
	private SeatGrade seatGrade;

	public SeatSection(String name, Stadium stadium, SeatGrade seatGrade) {
		this.name = name;
		this.stadium = stadium;
		this.seatGrade = seatGrade;
	}
}
