package com.goodseats.seatviewreviews.domain.member.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.goodseats.seatviewreviews.domain.BaseEntity;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
import com.goodseats.seatviewreviews.domain.point.model.entity.PointSavingLog;
import com.goodseats.seatviewreviews.domain.point.model.entity.PointUsingLog;

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

	@Column(name = "member_authority", length = 10, nullable = false)
	@Enumerated(EnumType.STRING)
	private MemberAuthority memberAuthority;

	@OneToMany(mappedBy = "member")
	private List<PointSavingLog> pointSavingLogs = new ArrayList<>();

	@OneToMany(mappedBy = "member")
	private List<PointUsingLog> pointUsingLogs = new ArrayList<>();

	public Member(String loginEmail, String password, String nickname) {
		this.loginEmail = loginEmail;
		this.password = password;
		this.nickname = nickname;
		this.point = 0;
		this.memberAuthority = MemberAuthority.USER;
	}

	public void addPointSavingLog(PointSavingLog pointSavingLog) {
		pointSavingLog.setMember(this);
	}

	public void addPointUsingLog(PointUsingLog pointUsingLog) {
		pointUsingLog.setMember(this);
	}

	public void savePoint(int amount) {
		this.point += amount;
	}

	public void usePoint(int amount) {
		if (amount > this.point) {
			throw new IllegalArgumentException("보유한 포인트가 부족합니다.");
		}

		this.point -= amount;
	}
}
