package com.goodseats.seatviewreviews.domain.vote.controller;

import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.goodseats.seatviewreviews.common.security.Authority;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.ReviewVoteCreateRequest;
import com.goodseats.seatviewreviews.domain.vote.service.ReviewVoteService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/votes")
public class ReviewVoteController {

	private final ReviewVoteService reviewVoteService;

	@Authority(authorities = {USER, ADMIN})
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Void> createVote(@Valid @RequestBody ReviewVoteCreateRequest reviewVoteCreateRequest) {
		reviewVoteService.createVote(reviewVoteCreateRequest);
		return ResponseEntity.noContent().build();
	}
}
