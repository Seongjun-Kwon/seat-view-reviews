package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatRepository;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private SeatRepository seatRepository;

	@InjectMocks
	private ReviewService reviewService;

	@Test
	@DisplayName("Success - 후기 생성에 성공한다")
	void createReviewSuccess() {
		// given
		Long seatId = 1L;
		Long memberId = 1L;
		Long reviewId = 1L;
		ReviewCreateRequest reviewCreateRequest = new ReviewCreateRequest(
				"테스트 제목", "테스트 내용", 5, seatId
		);

		Member member = new Member("test@test.com", "test", "test");
		ReflectionTestUtils.setField(member, "id", memberId);

		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		SeatSection seatSection = new SeatSection("110", stadium, seatGrade);
		Seat seat = new Seat("1", seatGrade, seatSection);
		ReflectionTestUtils.setField(seat, "id", seatId);

		Review review = new Review(
				reviewCreateRequest.title(), reviewCreateRequest.content(), reviewCreateRequest.score(), member, seat
		);
		ReflectionTestUtils.setField(review, "id", reviewId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
		when(reviewRepository.save(any(Review.class))).thenReturn(review);

		// when
		Long savedReviewId = reviewService.createReview(reviewCreateRequest, memberId);

		// then
		verify(memberRepository).findById(memberId);
		verify(seatRepository).findById(seatId);
		verify(reviewRepository).save(any(Review.class));
		assertThat(savedReviewId).isEqualTo(review.getId());
	}

	@Test
	@DisplayName("Fail - 후기 작성 하려는 좌석의 id 가 없으면 실패한다")
	void createReviewFailByNotFoundSeatId() {
		// given
		Long seatId = 1L;
		Long memberId = 1L;
		ReviewCreateRequest reviewCreateRequest = new ReviewCreateRequest(
				"테스트 제목", "테스트 내용", 5, seatId
		);

		Member member = new Member("test@test.com", "test", "test");
		ReflectionTestUtils.setField(member, "id", memberId);

		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		SeatSection seatSection = new SeatSection("110", stadium, seatGrade);
		Seat seat = new Seat("1", seatGrade, seatSection);
		ReflectionTestUtils.setField(seat, "id", seatId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(reviewCreateRequest, memberId))
				.isExactlyInstanceOf(NotFoundException.class)
				.hasMessage(NOT_FOUND.getMessage());
	}

}