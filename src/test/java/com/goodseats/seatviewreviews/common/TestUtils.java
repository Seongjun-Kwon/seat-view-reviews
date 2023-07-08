package com.goodseats.seatviewreviews.common;

import static com.goodseats.seatviewreviews.common.security.SessionConstant.*;

import org.springframework.mock.web.MockHttpSession;

import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;

public class TestUtils {

	private TestUtils() {
	}

	public static MockHttpSession getLoginSession(Member member, MemberAuthority memberAuthority) {
		AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), memberAuthority);
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(LOGIN_MEMBER_INFO, authenticationDTO);
		return session;
	}
}
