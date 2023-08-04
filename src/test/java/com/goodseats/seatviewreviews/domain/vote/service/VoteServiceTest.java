package com.goodseats.seatviewreviews.domain.vote.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static com.goodseats.seatviewreviews.domain.vote.model.vo.VoteChoice.*;
import static com.goodseats.seatviewreviews.domain.vote.model.vo.VoteType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.VoteCreateRequest;
import com.goodseats.seatviewreviews.domain.vote.model.entity.Vote;
import com.goodseats.seatviewreviews.domain.vote.repository.VoteRepository;
import com.goodseats.seatviewreviews.domain.vote.repository.VoteTypeRepository;
import com.goodseats.seatviewreviews.domain.vote.repository.VoteTypeRepositoryFactory;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

	@Mock
	private VoteRepository voteRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private VoteTypeRepositoryFactory voteTypeRepositoryFactory;

	@Mock
	private VoteTypeRepository voteTypeRepository;

	@InjectMocks
	private VoteService voteService;

	private Member member;
	private Stadium stadium;
	private SeatGrade seatGrade;
	private SeatSection seatSection;
	private Seat seat;
	private Review tempReview;
	private Review publishedReview;

	@BeforeEach
	void setUp() {
		member = new Member("test@test.com", "test", "test");
		stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		seatSection = new SeatSection("110", stadium, seatGrade);
		seat = new Seat("1", seatGrade, seatSection);
		tempReview = new Review(member, seat);
		publishedReview = new Review(member, seat);
		publishedReview.publish("테스트 제목", "테스트 내용", 5);

		ReflectionTestUtils.setField(member, "id", 1L);
		ReflectionTestUtils.setField(stadium, "id", 1L);
		ReflectionTestUtils.setField(seatGrade, "id", 1L);
		ReflectionTestUtils.setField(seatSection, "id", 1L);
		ReflectionTestUtils.setField(seat, "id", 1L);
		ReflectionTestUtils.setField(tempReview, "id", 1L);
		ReflectionTestUtils.setField(publishedReview, "id", 1L);
	}

	@Test
	@DisplayName("Success - 투표 생성에 성공한다")
	void createVoteSuccess() {
		// given
		VoteCreateRequest voteCreateRequest = new VoteCreateRequest(member.getId(), REVIEW, publishedReview.getId(), LIKE);

		when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
		when(voteTypeRepositoryFactory.createVoteTypeRepository(voteCreateRequest.voteType()))
				.thenReturn(voteTypeRepository);
		when(voteTypeRepository.existsByReferenceId(voteCreateRequest.referenceId())).thenReturn(true);
		when(voteRepository
				.existsByMemberAndVoteTypeAndReferenceId(member, voteCreateRequest.voteType(), voteCreateRequest.referenceId()))
				.thenReturn(false);
		when(voteRepository.save(any(Vote.class))).thenReturn(any(Vote.class));

		// when
		voteService.createVote(voteCreateRequest);

		// then
		verify(memberRepository).findById(member.getId());
		verify(voteTypeRepositoryFactory).createVoteTypeRepository(voteCreateRequest.voteType());
		verify(voteTypeRepository).existsByReferenceId(voteCreateRequest.referenceId());
		verify(voteRepository).existsByMemberAndVoteTypeAndReferenceId(
				member, voteCreateRequest.voteType(), voteCreateRequest.referenceId()
		);
		verify(voteRepository).save(any(Vote.class));
	}

	@Nested
	@DisplayName("createVoteFail")
	class CreateVoteFail {

		@Test
		@DisplayName("Fail - 투표하는 회원이 없는 회원이면 투표 생성에 실패한다")
		void createVoteFailByNotFoundMember() {
			// given
			Member wrongMember = new Member("test@test.com", "password", "wrongMember");
			ReflectionTestUtils.setField(wrongMember, "id", -1L);
			VoteCreateRequest voteCreateRequest = new VoteCreateRequest(wrongMember.getId(), REVIEW, publishedReview.getId(),
					LIKE);

			when(memberRepository.findById(wrongMember.getId())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> voteService.createVote(voteCreateRequest))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(memberRepository).findById(wrongMember.getId());
		}

		@Test
		@DisplayName("Fail - 투표하는 엔티티(후기, 댓글)가 없으면 투표 생성에 실패한다")
		void createVoteFailByNotFoundVoteType() {
			Review wrongReview = new Review(member, seat);
			ReflectionTestUtils.setField(wrongReview, "id", -1L);
			VoteCreateRequest voteCreateRequest = new VoteCreateRequest(member.getId(), REVIEW, wrongReview.getId(), LIKE);

			when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
			when(voteTypeRepositoryFactory.createVoteTypeRepository(voteCreateRequest.voteType()))
					.thenReturn(voteTypeRepository);
			when(voteTypeRepository.existsByReferenceId(voteCreateRequest.referenceId())).thenReturn(false);

			// when & then
			assertThatThrownBy(() -> voteService.createVote(voteCreateRequest))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(memberRepository).findById(member.getId());
			verify(voteTypeRepositoryFactory).createVoteTypeRepository(voteCreateRequest.voteType());
			verify(voteTypeRepository).existsByReferenceId(wrongReview.getId());
		}

		@Test
		@DisplayName("Fail - 이미 투표했으면 투표 생성에 실패한다")
		void createVoteFailByAlreadyVote() {
			VoteCreateRequest voteCreateRequest = new VoteCreateRequest(member.getId(), REVIEW, publishedReview.getId(), LIKE);

			when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
			when(voteTypeRepositoryFactory.createVoteTypeRepository(voteCreateRequest.voteType()))
					.thenReturn(voteTypeRepository);
			when(voteTypeRepository.existsByReferenceId(voteCreateRequest.referenceId())).thenReturn(true);
			when(voteRepository
					.existsByMemberAndVoteTypeAndReferenceId(member, voteCreateRequest.voteType(), voteCreateRequest.referenceId()))
					.thenReturn(true);

			// when & then
			assertThatThrownBy(() -> voteService.createVote(voteCreateRequest))
					.isExactlyInstanceOf(DuplicatedException.class)
					.hasMessage(ALREADY_VOTED.getMessage());

			verify(memberRepository).findById(member.getId());
			verify(voteTypeRepositoryFactory).createVoteTypeRepository(voteCreateRequest.voteType());
			verify(voteTypeRepository).existsByReferenceId(voteCreateRequest.referenceId());
			verify(voteRepository).existsByMemberAndVoteTypeAndReferenceId(
					member, voteCreateRequest.voteType(), voteCreateRequest.referenceId()
			);
		}
	}
}