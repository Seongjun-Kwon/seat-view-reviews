package com.goodseats.seatviewreviews.domain.reviewlike.model.entity;

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

import com.goodseats.seatviewreviews.domain.BaseEntity;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.model.vo.LikeType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewLike extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "like_type", length = 10, nullable = false)
	@Enumerated(value = EnumType.STRING)
	private LikeType likeType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "review_id")
	private Review review;

	public ReviewLike(LikeType likeType, Member member, Review review) {
		this.likeType = likeType;
		this.member = member;
		this.review = review;
	}
}
