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
@Table(name = "seat_grade")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SeatGrade extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", length = 30, nullable = false)
	private String name;

	@Column(name = "price_info")
	private String priceInfo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stadium_id")
	private Stadium stadium;

	public SeatGrade(String name, String priceInfo, Stadium stadium) {
		this.name = name;
		this.priceInfo = priceInfo;
		this.stadium = stadium;
	}
}
