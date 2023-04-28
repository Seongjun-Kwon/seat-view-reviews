package com.goodseats.seatviewreviews.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goodseats.seatviewreviews.domain.member.model.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByLoginEmail(String email);

	Optional<Member> findByLoginEmail(String email);

	boolean existsByNickname(String nickname);
}
