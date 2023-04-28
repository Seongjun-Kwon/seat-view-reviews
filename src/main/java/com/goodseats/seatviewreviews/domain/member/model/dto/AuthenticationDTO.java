package com.goodseats.seatviewreviews.domain.member.model.dto;

import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;

public record AuthenticationDTO(Long memberId, MemberAuthority memberAuthority) {
}
