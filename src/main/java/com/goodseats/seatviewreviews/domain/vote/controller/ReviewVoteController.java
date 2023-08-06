package com.goodseats.seatviewreviews.domain.vote.controller;

import static com.goodseats.seatviewreviews.common.security.SessionConstant.*;
import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.goodseats.seatviewreviews.common.security.Authority;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.ReviewVoteCreateRequest;
import com.goodseats.seatviewreviews.domain.vote.service.ReviewVoteService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviewvotes")
public class ReviewVoteController {

	private final ReviewVoteService reviewVoteService;

	@Authority(authorities = {USER, ADMIN})
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Void> createVote(
			@Valid @RequestBody ReviewVoteCreateRequest reviewVoteCreateRequest,
			@SessionAttribute(value = LOGIN_MEMBER_INFO) AuthenticationDTO authenticationDTO,
			HttpServletRequest request
	) {
		Long voteId = reviewVoteService.createVote(reviewVoteCreateRequest, authenticationDTO.memberId());
		return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + voteId)).build();
	}

	@Authority(authorities = {USER, ADMIN})
	@DeleteMapping("/{reviewVoteId}")
	ResponseEntity<Void> deleteVote(
			@PathVariable Long reviewVoteId,
			@SessionAttribute(value = LOGIN_MEMBER_INFO) AuthenticationDTO authenticationDTO
	) {
		reviewVoteService.deleteVote(reviewVoteId, authenticationDTO.memberId());
		return ResponseEntity.noContent().build();
	}
}
