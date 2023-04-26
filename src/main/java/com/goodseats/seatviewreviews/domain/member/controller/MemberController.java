package com.goodseats.seatviewreviews.domain.member.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goodseats.seatviewreviews.domain.member.model.dto.MemberSignUpRequest;
import com.goodseats.seatviewreviews.domain.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/sign-up")
	public ResponseEntity<Void> signUp(
			@Valid @ModelAttribute MemberSignUpRequest memberSignUpRequest,
			HttpServletRequest request
	) {
		Long savedMemberId = memberService.signUp(memberSignUpRequest);

		return ResponseEntity
				.created(URI.create(request.getRequestURI() + savedMemberId))
				.build();
	}
}

