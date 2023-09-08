package com.goodseats.seatviewreviews.domain.review.controller;

import static com.goodseats.seatviewreviews.common.security.SessionConstant.*;
import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;

import java.net.URI;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.goodseats.seatviewreviews.common.security.Authority;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.VoteCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.VotesGetRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.VotesResponse;
import com.goodseats.seatviewreviews.domain.review.service.VoteRedisFacade;
import com.goodseats.seatviewreviews.domain.review.service.VoteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/votes")
public class VoteController {

	private final VoteService voteService;
	private final VoteRedisFacade voteRedisFacade;

	@Authority(authorities = {USER, ADMIN})
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Void> createVote(
			@Valid @RequestBody VoteCreateRequest voteCreateRequest,
			@SessionAttribute(value = LOGIN_MEMBER_INFO) AuthenticationDTO authenticationDTO,
			HttpServletRequest request
	) {
		Long voteId = voteRedisFacade.createVote(voteCreateRequest, authenticationDTO.memberId());
		return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + voteId)).build();
	}

	@Authority(authorities = {USER, ADMIN})
	@DeleteMapping("/{voteId}")
	ResponseEntity<Void> deleteVote(
			@PathVariable Long voteId,
			@SessionAttribute(value = LOGIN_MEMBER_INFO) AuthenticationDTO authenticationDTO
	) {
		voteRedisFacade.deleteVote(voteId, authenticationDTO.memberId());
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	ResponseEntity<VotesResponse> getVotes(
			@RequestParam Long reviewId,
			@SessionAttribute(value = LOGIN_MEMBER_INFO, required = false) AuthenticationDTO authenticationDTO
	) {
		VotesGetRequest votesGetRequest = new VotesGetRequest(reviewId, Optional.ofNullable(authenticationDTO));
		VotesResponse votesResponse = voteService.getVotes(votesGetRequest);
		return ResponseEntity.ok(votesResponse);
	}
}
