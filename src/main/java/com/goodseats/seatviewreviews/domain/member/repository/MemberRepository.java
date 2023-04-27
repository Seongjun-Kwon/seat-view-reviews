package com.goodseats.seatviewreviews.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByLoginEmail(String email);

	boolean existsByNickname(String nickname);
}
