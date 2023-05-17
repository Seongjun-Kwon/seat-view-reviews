package com.goodseats.seatviewreviews.common.security;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static com.goodseats.seatviewreviews.common.security.SessionConstant.*;

import java.util.Arrays;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.goodseats.seatviewreviews.common.error.exception.AuthenticationException;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

	private final MemberRepository memberRepository;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

		HandlerMethod handlerMethod = (HandlerMethod)handler;
		Authority authorityAnnotation = handlerMethod.getMethodAnnotation(Authority.class);
		if (Objects.isNull(authorityAnnotation)) {
			return true;
		}

		HttpSession session = request.getSession(false);
		if (Objects.isNull(session)) {
			throw new AuthenticationException(UNAUTHORIZED);
		}

		AuthenticationDTO authenticationDTO = (AuthenticationDTO)session.getAttribute(LOGIN_MEMBER_INFO);
		if (Objects.isNull(authenticationDTO)) {
			throw new AuthenticationException(UNAUTHORIZED);
		}

		return checkAuthority(authorityAnnotation, authenticationDTO);
	}

	private boolean checkAuthority(Authority authorityAnnotation, AuthenticationDTO authenticationDTO) {
		if (isMemberInvalid(authenticationDTO.memberId())) {
			throw new AuthenticationException(UNAUTHORIZED);
		}

		boolean hasAuthority = Arrays.stream(authorityAnnotation.authorities())
				.anyMatch(authority -> authority.equals(authenticationDTO.memberAuthority()));
		if (hasAuthority) {
			return true;
		}

		throw new AuthenticationException(UNAUTHORIZED);
	}

	private boolean isMemberInvalid(Long memberId) {
		return !memberRepository.existsByIdAndDeletedAtIsNull(memberId);
	}
}
