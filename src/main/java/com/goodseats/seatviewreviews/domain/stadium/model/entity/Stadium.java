package com.goodseats.seatviewreviews.domain.stadium.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.goodseats.seatviewreviews.domain.BaseEntity;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stadium")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Stadium extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", length = 50, nullable = false)
	private String name;

	@Column(name = "address", length = 255, nullable = false)
	private String address;

	@Column(name = "home_team", length = 50, nullable = false)
	@Enumerated(value = EnumType.STRING)
	private HomeTeam homeTeam;

	public Stadium(String name, String address, HomeTeam homeTeam) {
		this.name = name;
		this.address = address;
		this.homeTeam = homeTeam;
	}
}
