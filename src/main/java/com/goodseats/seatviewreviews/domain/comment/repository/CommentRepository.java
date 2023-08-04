package com.goodseats.seatviewreviews.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goodseats.seatviewreviews.domain.comment.model.entity.Comment;
import com.goodseats.seatviewreviews.domain.vote.repository.VoteTypeRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, VoteTypeRepository {

	@Override
	@Query(value = "SELECT COUNT(c) > 0 FROM Comment c WHERE c.id = :referenceId")
	boolean existsByReferenceId(@Param(value = "referenceId") Long referenceId);
}
