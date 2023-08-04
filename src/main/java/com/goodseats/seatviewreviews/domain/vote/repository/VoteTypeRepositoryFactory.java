package com.goodseats.seatviewreviews.domain.vote.repository;

import org.springframework.stereotype.Component;

import com.goodseats.seatviewreviews.domain.comment.repository.CommentRepository;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.vote.model.vo.VoteType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VoteTypeRepositoryFactory {

	private final ReviewRepository reviewRepository;
	private final CommentRepository commentRepository;

	public VoteTypeRepository createVoteTypeRepository(VoteType voteType) {
		return
				switch (voteType) {
					case REVIEW -> reviewRepository;
					case COMMENT -> commentRepository;
				};
	}
}
