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

	@Column(name = "title", length = 50, nullable = false)
	private String title;

	@Column(name = "content", length = 2000, nullable = false)
	private String content;

	@Column(name = "score", nullable = false)
	private int score;

	@Column(name = "hits", nullable = false)
	private int hits;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "seat_id")
	private Seat seat;

	public Review(String title, String content, int score, int hits, Member member, Seat seat) {
		this.title = title;
		this.content = content;
		this.score = score;
		this.hits = hits;
		this.member = member;
		this.seat = seat;
	}
}
