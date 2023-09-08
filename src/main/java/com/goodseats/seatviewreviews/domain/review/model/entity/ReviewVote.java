package com.goodseats.seatviewreviews.domain.review.model.entity;

import static com.goodseats.seatviewreviews.domain.review.model.vo.VoteChoice.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import com.goodseats.seatviewreviews.domain.review.model.vo.VoteChoice;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_vote")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewVote extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vote_choice", length = 10, nullable = false)
	@Enumerated(value = EnumType.STRING)
	private VoteChoice voteChoice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_id")
	private Review review;

	public ReviewVote(VoteChoice voteChoice, Member member, Review review) {
		this.voteChoice = voteChoice;
		this.member = member;
		this.review = review;
	}

	public void verifyVoter(Long memberId) {
		if (!this.member.getId().equals(memberId)) {
			throw new AuthenticationException(ErrorCode.UNAUTHORIZED);
		}
	}

	public boolean isLike() {
		return this.voteChoice == LIKE;
	}

	public boolean isDislike() {
		return this.voteChoice == DISLIKE;
	}
}
