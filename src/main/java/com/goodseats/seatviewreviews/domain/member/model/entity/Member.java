package com.goodseats.seatviewreviews.domain.member.model.entity;

import java.time.LocalDate;

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

	@Column(name = "login_id", length = 20, nullable = false)
	private String loginId;

	@Column(name = "password", length = 20, nullable = false)
	private String password;

	@Column(name = "name", length = 6, nullable = false)
	private String name;

	@Column(name = "birth", nullable = false)
	private LocalDate birth;

	@Column(name = "email", length = 50, nullable = false, unique = true)
	private String email;

	@Column(name = "nickname", length = 10, nullable = false, unique = true)
	private String nickname;

	@Column(name = "point", nullable = false)
	private int point;

	public Member(String loginId, String password, String name, LocalDate birth, String email, String nickname) {
		this.loginId = loginId;
		this.password = password;
		this.name = name;
		this.birth = birth;
		this.email = email;
		this.nickname = nickname;
		this.point = 0;
	}
}
