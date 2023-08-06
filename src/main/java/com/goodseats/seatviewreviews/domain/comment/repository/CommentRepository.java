package com.goodseats.seatviewreviews.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.comment.model.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}