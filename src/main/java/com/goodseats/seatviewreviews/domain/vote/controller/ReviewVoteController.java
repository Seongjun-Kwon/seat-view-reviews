package com.goodseats.seatviewreviews.domain.vote.controller;

import static com.goodseats.seatviewreviews.common.security.SessionConstant.*;
import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;

import java.net.URI;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.goodseats.seatviewreviews.common.security.Authority;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.ReviewVoteCreateRequest;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.ReviewVotesGetRequest;
import com.goodseats.seatviewreviews.domain.vote.model.dto.response.ReviewVotesResponse;
import com.goodseats.seatviewreviews.domain.vote.service.ReviewVoteRedisFacade;
import com.goodseats.seatviewreviews.domain.vote.service.ReviewVoteService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviewvotes")
public class ReviewVoteController {

	private final ReviewVoteService reviewVoteService;
	private final ReviewVoteRedisFacade reviewVoteRedisFacade;

	@Authority(authorities = {USER, ADMIN})
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Void> createVote(
			@Valid @RequestBody ReviewVoteCreateRequest reviewVoteCreateRequest,
			@SessionAttribute(value = LOGIN_MEMBER_INFO) AuthenticationDTO authenticationDTO,
			HttpServletRequest request
	) {
		Long voteId = reviewVoteRedisFacade.createVote(reviewVoteCreateRequest, authenticationDTO.memberId());
		return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + voteId)).build();
	}

	@Authority(authorities = {USER, ADMIN})
	@DeleteMapping("/{reviewVoteId}")
	ResponseEntity<Void> deleteVote(
			@PathVariable Long reviewVoteId,
			@SessionAttribute(value = LOGIN_MEMBER_INFO) AuthenticationDTO authenticationDTO
	) {
		reviewVoteRedisFacade.deleteVote(reviewVoteId, authenticationDTO.memberId());
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	ResponseEntity<ReviewVotesResponse> getVotes(
			@RequestParam Long reviewId,
			@SessionAttribute(value = LOGIN_MEMBER_INFO, required = false) AuthenticationDTO authenticationDTO
	) {
		ReviewVotesGetRequest reviewVotesGetRequest = new ReviewVotesGetRequest(
				reviewId, Optional.ofNullable(authenticationDTO)
		);
		ReviewVotesResponse reviewVotesResponse = reviewVoteService.getVotes(reviewVotesGetRequest);
		return ResponseEntity.ok(reviewVotesResponse);
	}
}
