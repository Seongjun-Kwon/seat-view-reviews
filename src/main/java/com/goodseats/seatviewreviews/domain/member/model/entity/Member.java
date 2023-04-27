package com.goodseats.seatviewreviews.domain.member.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.goodseats.seatviewreviews.domain.BaseEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "login_email", length = 50, nullable = false, unique = true)
	private String loginEmail;

	@Column(name = "password", length = 255, nullable = false)
	private String password;

	@Column(name = "nickname", length = 10, nullable = false, unique = true)
	private String nickname;

	@Column(name = "point", nullable = false)
	private int point;

	public Member(String loginEmail, String password, String nickname) {
		this.loginEmail = loginEmail;
		this.password = password;
		this.nickname = nickname;
		this.point = 0;
	}
}
