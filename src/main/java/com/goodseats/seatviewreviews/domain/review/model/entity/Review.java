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
import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.common.error.exception.ErrorCode;
import com.goodseats.seatviewreviews.domain.BaseEntity;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.vo.VoteChoice;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Seat;

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

	@Column(name = "published", nullable = false)
	private boolean published;

	@Column(name = "like_count", nullable = false)
	private int likeCount;

	@Column(name = "dislike_count", nullable = false)
	private int dislikeCount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id")
	private Seat seat;

	public Review(Member member, Seat seat) {
		this.viewCount = 0;
		this.published = false;
		this.member = member;
		this.seat = seat;
		this.likeCount = 0;
		this.dislikeCount = 0;
	}

	public void publish(String title, String content, int score) {
		if (this.published) {
			throw new DuplicatedException(ErrorCode.ALREADY_PUBLISHED);
		}

		this.title = title;
		this.content = content;
		this.score = score;
		this.published = true;
	}

	public void verifyWriter(Long memberId) {
		if (!this.member.getId().equals(memberId)) {
			throw new AuthenticationException(ErrorCode.UNAUTHORIZED);
		}
	}

	public void updateViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public void updateVoteCount(int voteCount, VoteChoice voteChoice) {
		switch (voteChoice) {
			case LIKE -> this.likeCount=voteCount;
			case DISLIKE -> this.dislikeCount=voteCount;
		}
	}
}