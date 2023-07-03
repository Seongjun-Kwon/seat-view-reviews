package com.goodseats.seatviewreviews.domain.review.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.goodseats.seatviewreviews.common.error.exception.AuthenticationException;
import com.goodseats.seatviewreviews.common.error.exception.ErrorCode;
import com.goodseats.seatviewreviews.domain.BaseEntity;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", length = 50, nullable = true)
	private String title;

	@Column(name = "content", length = 10000, nullable = true)
	private String content;

	@Column(name = "score", nullable = true)
	private int score;

	@Column(name = "view_count", nullable = false)
	private int viewCount;

	@Column(name = "is_published", nullable = false)
	private boolean isPublished;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id")
	private Seat seat;

	public Review(String title, String content, int score, Member member, Seat seat) {
		this.title = title;
		this.content = content;
		this.score = score;
		this.viewCount = 0;
		this.isPublished = false;
		this.member = member;
		this.seat = seat;
	}

	public Review(Member member, Seat seat) {
		this.viewCount = 0;
		this.isPublished = false;
		this.member = member;
		this.seat = seat;
	}

	public void publish(String title, String content, int score) {
		this.title = title;
		this.content = content;
		this.score = score;
		this.isPublished = true;
	}

	public void verifyWriter(Long memberId) {
		if (!this.member.getId().equals(memberId)) {
			throw new AuthenticationException(ErrorCode.UNAUTHORIZED);
		}
	}

	public void updateViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
}