package com.goodseats.seatviewreviews.domain.vote.model.entity;

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
import com.goodseats.seatviewreviews.domain.vote.model.vo.VoteChoice;
import com.goodseats.seatviewreviews.domain.vote.model.vo.VoteType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vote")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Vote extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vote_type", length = 10, nullable = false)
	@Enumerated(value = EnumType.STRING)
	private VoteType voteType;

	@Column(name = "reference_id", nullable = false)
	private Long referenceId;

	@Column(name = "vote_choice", length = 10, nullable = false)
	@Enumerated(value = EnumType.STRING)
	private VoteChoice voteChoice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public Vote(VoteType voteType, Long referenceId, VoteChoice voteChoice, Member member) {
		this.voteType = voteType;
		this.referenceId = referenceId;
		this.voteChoice = voteChoice;
		this.member = member;
	}
}
