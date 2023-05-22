package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.goodseats.seatviewreviews.common.error.exception.AuthenticationException;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewPublishRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.TempReviewCreateRequest;
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
	@DisplayName("Success - 후기 임시 생성에 성공한다")
	void createTempReviewSuccess() {
		// given
		Long seatId = 1L;
		Long memberId = 1L;
		Long reviewId = 1L;
		TempReviewCreateRequest tempReviewCreateRequest = new TempReviewCreateRequest(seatId);

		Member member = new Member("test@test.com", "test", "test");
		ReflectionTestUtils.setField(member, "id", memberId);

		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		SeatSection seatSection = new SeatSection("110", stadium, seatGrade);
		Seat seat = new Seat("1", seatGrade, seatSection);
		ReflectionTestUtils.setField(seat, "id", seatId);

		Review review = new Review(member, seat);
		ReflectionTestUtils.setField(review, "id", reviewId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
		when(reviewRepository.save(any(Review.class))).thenReturn(review);

		// when
		Long savedReviewId = reviewService.createTempReview(tempReviewCreateRequest, memberId);

		// then
		verify(memberRepository).findById(memberId);
		verify(seatRepository).findById(seatId);
		verify(reviewRepository).save(any(Review.class));
		assertThat(savedReviewId).isEqualTo(review.getId());
	}

	@Test
	@DisplayName("Fail - 후기 작성 하려는 좌석의 id 가 없으면 실패한다")
	void createTempReviewFailByNotFoundSeatId() {
		// given
		Long seatId = 1L;
		Long memberId = 1L;
		TempReviewCreateRequest tempReviewCreateRequest = new TempReviewCreateRequest(seatId);

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
		assertThatThrownBy(() -> reviewService.createTempReview(tempReviewCreateRequest, memberId))
				.isExactlyInstanceOf(NotFoundException.class)
				.hasMessage(NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("Success - 후기 발행에 성공한다")
	void publishReviewSuccess() {
		// given
		Long seatId = 1L;
		Long memberId = 1L;
		Long reviewId = 1L;
		ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

		Member member = new Member("test@test.com", "test", "test");
		ReflectionTestUtils.setField(member, "id", memberId);

		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		SeatSection seatSection = new SeatSection("110", stadium, seatGrade);
		Seat seat = new Seat("1", seatGrade, seatSection);
		ReflectionTestUtils.setField(seat, "id", seatId);

		Review tempReview = new Review(member, seat);
		ReflectionTestUtils.setField(tempReview, "id", reviewId);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(tempReview));

		// when
		reviewService.publishReview(reviewPublishRequest, reviewId, memberId);

		// then
		assertThat(tempReview.getTitle()).isEqualTo(reviewPublishRequest.title());
		assertThat(tempReview.getContent()).isEqualTo(reviewPublishRequest.content());
		assertThat(tempReview.getScore()).isEqualTo(reviewPublishRequest.score());
	}

	@Nested
	@DisplayName("publishReviewFail")
	class PublishReviewFail {

		@Test
		@DisplayName("Fail - 발행하고자 하는 임시 후기가 존재하지 않으면 실패한다")
		void publishReviewFailByNotFoundTempReview() {
			// given
			Long memberId = 1L;
			Long reviewId = 1L;
			ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

			when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> reviewService.publishReview(reviewPublishRequest, reviewId, memberId))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("Fail - 발행하는 회원이 임시 후기를 작성한 회원이 아니면 실패한다")
		void publishReviewFailByNotTempReviewWriter() {
			Long seatId = 1L;
			Long memberId = 1L;
			Long wrongMemberId = 2L;
			Long reviewId = 1L;
			ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

			Member member = new Member("test@test.com", "test", "test");
			ReflectionTestUtils.setField(member, "id", memberId);

			Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
			SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
			SeatSection seatSection = new SeatSection("110", stadium, seatGrade);
			Seat seat = new Seat("1", seatGrade, seatSection);
			ReflectionTestUtils.setField(seat, "id", seatId);

			Review tempReview = new Review(member, seat);
			ReflectionTestUtils.setField(tempReview, "id", reviewId);

			when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(tempReview));

			// when & then
			assertThatThrownBy(() -> reviewService.publishReview(reviewPublishRequest, reviewId, wrongMemberId))
					.isExactlyInstanceOf(AuthenticationException.class)
					.hasMessage(UNAUTHORIZED.getMessage());
		}
	}
}