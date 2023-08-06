package com.goodseats.seatviewreviews.domain.vote.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static com.goodseats.seatviewreviews.domain.vote.model.vo.VoteChoice.*;
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
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.ReviewVoteCreateRequest;
import com.goodseats.seatviewreviews.domain.vote.model.entity.ReviewVote;
import com.goodseats.seatviewreviews.domain.vote.repository.ReviewVoteRepository;

@ExtendWith(MockitoExtension.class)
class ReviewVoteServiceTest {

	@Mock
	private ReviewVoteRepository reviewVoteRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@InjectMocks
	private ReviewVoteService reviewVoteService;

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
	@DisplayName("Success - 후기 투표 생성에 성공한다")
	void createVoteSuccess() {
		// given
		ReviewVoteCreateRequest reviewVoteCreateRequest
				= new ReviewVoteCreateRequest(member.getId(), publishedReview.getId(), LIKE);

		when(memberRepository.findById(reviewVoteCreateRequest.memberId())).thenReturn(Optional.of(member));
		when(reviewRepository.findById(reviewVoteCreateRequest.reviewId())).thenReturn(Optional.of(publishedReview));
		when(reviewVoteRepository.existsByMemberAndReview(member, publishedReview)).thenReturn(false);
		when(reviewVoteRepository.save(any(ReviewVote.class))).thenReturn(any(ReviewVote.class));

		// when
		reviewVoteService.createVote(reviewVoteCreateRequest);

		// then
		verify(memberRepository).findById(reviewVoteCreateRequest.memberId());
		verify(reviewRepository).findById(reviewVoteCreateRequest.reviewId());
		verify(reviewVoteRepository).existsByMemberAndReview(member, publishedReview);
		verify(reviewVoteRepository).save(any(ReviewVote.class));
	}

	@Nested
	@DisplayName("createVoteFail")
	class CreateReviewVoteFail {

		@Test
		@DisplayName("Fail - 투표하는 회원이 없는 회원이면 후기 투표 생성에 실패한다")
		void createVoteFailByNotFoundMember() {
			// given
			Member wrongMember = new Member("test@test.com", "password", "wrongMember");
			ReflectionTestUtils.setField(wrongMember, "id", -1L);
			ReviewVoteCreateRequest reviewVoteCreateRequest
					= new ReviewVoteCreateRequest(wrongMember.getId(), publishedReview.getId(), LIKE);

			when(memberRepository.findById(reviewVoteCreateRequest.memberId())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> reviewVoteService.createVote(reviewVoteCreateRequest))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(memberRepository).findById(reviewVoteCreateRequest.memberId());
		}

		@Test
		@DisplayName("Fail - 투표하는 엔티티(후기, 댓글)가 없으면 후기 투표 생성에 실패한다")
		void createVoteFailByNotFoundVoteType() {
			Review wrongReview = new Review(member, seat);
			ReflectionTestUtils.setField(wrongReview, "id", -1L);
			ReviewVoteCreateRequest reviewVoteCreateRequest
					= new ReviewVoteCreateRequest(member.getId(), wrongReview.getId(), LIKE);

			when(memberRepository.findById(reviewVoteCreateRequest.memberId())).thenReturn(Optional.of(member));
			when(reviewRepository.findById(reviewVoteCreateRequest.reviewId())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> reviewVoteService.createVote(reviewVoteCreateRequest))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(memberRepository).findById(reviewVoteCreateRequest.memberId());
			verify(reviewRepository).findById(reviewVoteCreateRequest.reviewId());
		}

		@Test
		@DisplayName("Fail - 이미 투표했으면 후기 투표 생성에 실패한다")
		void createVoteFailByAlreadyVote() {
			ReviewVoteCreateRequest reviewVoteCreateRequest
					= new ReviewVoteCreateRequest(member.getId(), publishedReview.getId(), LIKE);

			when(memberRepository.findById(reviewVoteCreateRequest.memberId())).thenReturn(Optional.of(member));
			when(reviewRepository.findById(reviewVoteCreateRequest.reviewId())).thenReturn(Optional.of(publishedReview));
			when(reviewVoteRepository.existsByMemberAndReview(member, publishedReview)).thenReturn(true);

			// when & then
			assertThatThrownBy(() -> reviewVoteService.createVote(reviewVoteCreateRequest))
					.isExactlyInstanceOf(DuplicatedException.class)
					.hasMessage(ALREADY_VOTED.getMessage());

			verify(memberRepository).findById(reviewVoteCreateRequest.memberId());
			verify(reviewRepository).findById(reviewVoteCreateRequest.reviewId());
			verify(reviewVoteRepository).existsByMemberAndReview(member, publishedReview);
		}
	}
}