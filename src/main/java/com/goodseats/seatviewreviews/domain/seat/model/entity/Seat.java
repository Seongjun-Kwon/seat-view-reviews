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
@Table(name = "seat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Seat extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "section", length = 10, nullable = false)
	private String section;

	@Column(name = "seat_nubmer", nullable = false)
	private int seatNumber;

	@Column(name = "price", nullable = false)
	private int price;

	@Column(name = "average_score", nullable = false)
	private float averageScore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stadium_id")
	private Stadium stadium;

	public Seat(String section, int seatNumber, float averageScore, Stadium stadium) {
		this.section = section;
		this.seatNumber = seatNumber;
		this.averageScore = averageScore;
		this.stadium = stadium;
	}
}
