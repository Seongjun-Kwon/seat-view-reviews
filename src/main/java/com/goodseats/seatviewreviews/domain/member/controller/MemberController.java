package com.goodseats.seatviewreviews.domain.member.controller;

import static com.goodseats.seatviewreviews.common.security.SessionConstant.*;
import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;

import java.net.URI;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goodseats.seatviewreviews.common.security.Authority;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.member.model.dto.request.MemberLoginRequest;
import com.goodseats.seatviewreviews.domain.member.model.dto.request.MemberSignUpRequest;
import com.goodseats.seatviewreviews.domain.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

	private final MemberService memberService;

	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<Void> signUp(
			@Valid @ModelAttribute MemberSignUpRequest memberSignUpRequest,
			HttpServletRequest request
	) {
		Long savedMemberId = memberService.signUp(memberSignUpRequest);

		return ResponseEntity
				.created(URI.create(request.getRequestURI() + "/" + savedMemberId))
				.build();
	}

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<Void> login(
			@Valid @ModelAttribute MemberLoginRequest memberLoginRequest,
			HttpServletRequest request
	) {
		AuthenticationDTO authenticationDto = memberService.login(memberLoginRequest);

		HttpSession session = request.getSession(true);
		session.setAttribute(LOGIN_MEMBER_INFO, authenticationDto);

		return ResponseEntity.noContent().build();
	}

	@Authority(authorities = {USER, ADMIN})
	@PostMapping(value = "/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);

		if (Objects.nonNull(session)) {
			session.invalidate();
		}

		return ResponseEntity.noContent().build();
	}
}

