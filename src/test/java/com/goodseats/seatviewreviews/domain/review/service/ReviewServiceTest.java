package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.goodseats.seatviewreviews.common.error.exception.AuthenticationException;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewPublishRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.TempReviewCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewDetailResponse;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewsResponse;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.stadium.repository.SeatRepository;

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
	@DisplayName("Success - 후기 임시 생성에 성공한다")
	void createTempReviewSuccess() {
		// given
		TempReviewCreateRequest tempReviewCreateRequest = new TempReviewCreateRequest(seat.getId());

		when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
		when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));
		when(reviewRepository.save(any(Review.class))).thenReturn(tempReview);

		// when
		Long savedReviewId = reviewService.createTempReview(tempReviewCreateRequest, member.getId());

		// then
		verify(memberRepository).findById(member.getId());
		verify(seatRepository).findById(seat.getId());
		verify(reviewRepository).save(any(Review.class));
		assertThat(savedReviewId).isEqualTo(tempReview.getId());
	}

	@Test
	@DisplayName("Fail - 후기 작성 하려는 좌석의 id 가 없으면 실패한다")
	void createTempReviewFailByNotFoundSeatId() {
		// given
		TempReviewCreateRequest tempReviewCreateRequest = new TempReviewCreateRequest(seat.getId());

		when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
		when(seatRepository.findById(seat.getId())).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewService.createTempReview(tempReviewCreateRequest, member.getId()))
				.isExactlyInstanceOf(NotFoundException.class)
				.hasMessage(NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("Success - 후기 발행에 성공한다")
	void publishReviewSuccess() {
		// given
		ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

		when(reviewRepository.findById(tempReview.getId())).thenReturn(Optional.of(tempReview));

		// when
		reviewService.publishReview(reviewPublishRequest, tempReview.getId(), member.getId());

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
			ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

			when(reviewRepository.findById(tempReview.getId())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> reviewService.publishReview(reviewPublishRequest, tempReview.getId(), member.getId()))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("Fail - 발행하는 회원이 임시 후기를 작성한 회원이 아니면 실패한다")
		void publishReviewFailByNotTempReviewWriter() {
			Long wrongMemberId = 2L;
			ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

			when(reviewRepository.findById(tempReview.getId())).thenReturn(Optional.of(tempReview));

			// when & then
			assertThatThrownBy(() -> reviewService.publishReview(reviewPublishRequest, tempReview.getId(), wrongMemberId))
					.isExactlyInstanceOf(AuthenticationException.class)
					.hasMessage(UNAUTHORIZED.getMessage());
		}
	}

	@Test
	@DisplayName("Success - 후기 상세 조회에 성공한다")
	void getReviewSuccess() {
		// given
		when(reviewRepository.findById(publishedReview.getId())).thenReturn(Optional.of(publishedReview));

		// when
		ReviewDetailResponse reviewDetailResponse = reviewService.getReview(publishedReview.getId());

		// then
		verify(reviewRepository).findById(publishedReview.getId());
		assertThat(reviewDetailResponse.title()).isEqualTo(publishedReview.getTitle());
		assertThat(reviewDetailResponse.content()).isEqualTo(publishedReview.getContent());
		assertThat(reviewDetailResponse.viewCount()).isEqualTo(publishedReview.getViewCount());
		assertThat(reviewDetailResponse.score()).isEqualTo(publishedReview.getScore());
		assertThat(reviewDetailResponse.writer()).isEqualTo(publishedReview.getMember().getNickname());
	}

	@Test
	@DisplayName("Fail - 조회하려는 후기가 없으면 후기 상세 조회에 실패한다")
	void getReviewFailByNotFound() {
		// given
		when(reviewRepository.findById(publishedReview.getId())).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewService.getReview(publishedReview.getId()))
				.isExactlyInstanceOf(NotFoundException.class)
				.hasMessage(NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("Success - 특정 좌석의 후기 목록 조회에 성공한다")
	void getReviewsSuccess() {
		// given
		Review review1 = new Review(member, seat);
		Review review2 = new Review(member, seat);
		review1.publish("테스트 제목1", "테스트 내용1", 5);
		review2.publish("테스트 제목2", "테스트 내용2", 5);
		ReflectionTestUtils.setField(review1, "id", 1L);
		ReflectionTestUtils.setField(review2, "id", 2L);
		List<Review> reviews = List.of(review1, review2);
		PageImpl<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 10), reviews.size());

		when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));
		when(reviewRepository.findAllWithFetchMemberBySeatIdAndPublishedTrue(eq(seat.getId()), any(PageRequest.class)))
				.thenReturn(reviewPage);

		// when
		ReviewsResponse reviewsResponse = reviewService.getReviews(seat.getId(), PageRequest.of(0, 10));

		// then
		verify(seatRepository).findById(seat.getId());
		verify(reviewRepository).findAllWithFetchMemberBySeatIdAndPublishedTrue(eq(seat.getId()), any(PageRequest.class));
		assertThat(reviewsResponse.reviews().size()).isEqualTo(reviewPage.getTotalElements());
	}

	@Test
	@DisplayName("Fail - 후기 목록을 조회하려는 좌석이 존재하지 않는 좌석이면 실패한다")
	void getReviewsFailByNotFoundSeat() {
		// given
		when(seatRepository.findById(seat.getId())).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewService.getReviews(seat.getId(), PageRequest.of(0, 10)))
				.isExactlyInstanceOf(NotFoundException.class)
				.hasMessage(NOT_FOUND.getMessage());
		verify(seatRepository).findById(seat.getId());
	}
}