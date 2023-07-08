package com.goodseats.seatviewreviews.domain.review.controller;

import static com.goodseats.seatviewreviews.common.constant.CookieConstant.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodseats.seatviewreviews.common.TestUtils;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewPublishRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.TempReviewCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.review.service.ReviewRedisFacade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatGradeRepository;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatRepository;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatSectionRepository;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.stadium.repository.StadiumRepository;

@ActiveProfiles("test")
// @Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ReviewRedisFacade reviewRedisFacade;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StadiumRepository stadiumRepository;

	@Autowired
	private SeatGradeRepository seatGradeRepository;

	@Autowired
	private SeatSectionRepository seatSectionRepository;

	@Autowired
	private SeatRepository seatRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	private Member writer;
	private Member notWriter;
	private Stadium stadium;
	private SeatGrade seatGrade;
	private SeatSection seatSection;
	private Seat seat;
	private Review tempReview;
	private Review publishedReview;

	@BeforeEach
	void setUp() {
		writer = new Member("writer@test.com", "test", "writer");
		notWriter = new Member("notWriter@test.com", "test", "notWriter");
		stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		seatSection = new SeatSection("110", stadium, seatGrade);
		seat = new Seat("1", seatGrade, seatSection);
		tempReview = new Review(writer, seat);
		publishedReview = new Review("테스트 제목", "테스트 내용", 5, writer, seat);

		memberRepository.save(writer);
		memberRepository.save(notWriter);
		stadiumRepository.save(stadium);
		seatGradeRepository.save(seatGrade);
		seatSectionRepository.save(seatSection);
		seatRepository.save(seat);
		reviewRepository.save(tempReview);
		reviewRepository.save(publishedReview);
	}

	@AfterEach
	void clear() {
		memberRepository.delete(writer);
		memberRepository.delete(notWriter);
		stadiumRepository.delete(stadium);
		seatGradeRepository.delete(seatGrade);
		seatSectionRepository.delete(seatSection);
		seatRepository.delete(seat);
		reviewRepository.deleteAll();
	}

	@Test
	@DisplayName("Success - 후기 임시 생성에 성공하고 200 응답한다")
	void createTempReviewSuccess() throws Exception {
		// given
		MockHttpSession session = TestUtils.getLoginSession(writer, MemberAuthority.USER);
		TempReviewCreateRequest tempReviewCreateRequest = new TempReviewCreateRequest(seat.getId());

		// when & then
		mockMvc.perform(post("/api/v1/reviews")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(tempReviewCreateRequest)))
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("Fail - 후기 작성 하려는 좌석의 id 가 없으면 실패하고 404 응답한다")
	void createTempReviewFailByNotFoundSeatId() throws Exception {
		// given
		MockHttpSession session = TestUtils.getLoginSession(writer, MemberAuthority.USER);

		Long wrongSeatId = 0L;
		TempReviewCreateRequest tempReviewCreateRequest = new TempReviewCreateRequest(wrongSeatId);

		// when & then
		mockMvc.perform(post("/api/v1/reviews")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(tempReviewCreateRequest)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Success - 후기 발행에 성공하고 204 응답한다")
	void publishReviewSuccess() throws Exception {
		// given
		MockHttpSession session = TestUtils.getLoginSession(writer, MemberAuthority.USER);
		ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

		// when & then
		mockMvc.perform(patch("/api/v1/reviews/{reviewId}", tempReview.getId())
						.session(session)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("title", reviewPublishRequest.title())
						.param("content", reviewPublishRequest.content())
						.param("score", String.valueOf(reviewPublishRequest.score())))
				.andExpect(status().isNoContent());
	}

	@Nested
	@DisplayName("publishReviewFail")
	class PublishReviewFail {

		@Test
		@DisplayName("Fail - 발행하고자 하는 임시 후기가 존재하지 않으면 실패하고 404 응답한다")
		void publishReviewFailByNotFoundTempReview() throws Exception {
			MockHttpSession session = TestUtils.getLoginSession(writer, MemberAuthority.USER);

			Long wrongReviewId = 1L;
			ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

			// when & then
			mockMvc.perform(patch("/api/v1/reviews/{reviewId}", wrongReviewId)
							.session(session)
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.param("title", reviewPublishRequest.title())
							.param("content", reviewPublishRequest.content())
							.param("score", String.valueOf(reviewPublishRequest.score())))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("Fail - 발행하는 회원이 임시 후기를 작성한 회원이 아니면 실패하고 401 응답한다")
		void publishReviewFailByNotTempReviewWriter() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(notWriter, MemberAuthority.USER);
			ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

			// when & then
			mockMvc.perform(patch("/api/v1/reviews/{reviewId}", tempReview.getId())
							.session(session)
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.param("title", reviewPublishRequest.title())
							.param("content", reviewPublishRequest.content())
							.param("score", String.valueOf(reviewPublishRequest.score())))
					.andExpect(status().isUnauthorized());
		}
	}

	@Nested
	@DisplayName("getReviewSuccess")
	class getReviewSuccess {
		@Test
		@DisplayName("Success - 처음 조회하는 후기의 상세 조회에 성공하고 200 응답한다")
		void getReviewSuccessWhenFirstRead() throws Exception {
			// given & when & then
			mockMvc.perform(get("/api/v1/reviews/{reviewId}", publishedReview.getId())
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("title").value(publishedReview.getTitle()))
					.andExpect(jsonPath("content").value(publishedReview.getContent()))
					.andExpect(jsonPath("score").value(publishedReview.getScore()))
					.andExpect(jsonPath("viewCount").value(publishedReview.getViewCount()))
					.andExpect(jsonPath("writer").value(publishedReview.getMember().getNickname()))
					.andExpect(cookie().exists(USER_KEY))
					.andDo(print());
		}

		@Test
		@DisplayName("Success - 이미 조회한 후기의 상세 조회에 성공하고 200 응답한다")
		void getReviewSuccessWhenAlreadyRead() throws Exception {
			// given
			String userCookieValue = String.valueOf(UUID.randomUUID());
			MockCookie cookie = new MockCookie(USER_KEY, userCookieValue);

			// when & then
			mockMvc.perform(get("/api/v1/reviews/{reviewId}", publishedReview.getId())
							.accept(MediaType.APPLICATION_JSON)
							.cookie(cookie))
					.andExpect(status().isOk())
					.andExpect(jsonPath("title").value(publishedReview.getTitle()))
					.andExpect(jsonPath("content").value(publishedReview.getContent()))
					.andExpect(jsonPath("score").value(publishedReview.getScore()))
					.andExpect(jsonPath("viewCount").value(publishedReview.getViewCount()))
					.andExpect(jsonPath("writer").value(publishedReview.getMember().getNickname()))
					.andExpect(cookie().value(USER_KEY, userCookieValue))
					.andDo(print());
		}

		@Test
		@DisplayName("Success - 사용자가 이미 조회 한 후기를 조회 시 조회 수가 늘어나지 않는다")
		void notIncreaseViewCountWhenAlreadyRead() throws Exception {
			// given
			String userCookieValue = String.valueOf(UUID.randomUUID());
			MockCookie cookie = new MockCookie(USER_KEY, userCookieValue);

			mockMvc.perform(get("/api/v1/reviews/{reviewId}", publishedReview.getId())
							.accept(MediaType.APPLICATION_JSON)
							.cookie(cookie))
					.andDo(print());

			int viewCountBeforeRead
					= reviewRedisFacade.getLatestViewCount(publishedReview.getId(), publishedReview.getViewCount());

			// when
			mockMvc.perform(get("/api/v1/reviews/{reviewId}", publishedReview.getId())
							.accept(MediaType.APPLICATION_JSON)
							.cookie(cookie))
					.andExpect(status().isOk())
					.andDo(print());

			// then
			int viewCountAfterRead
					= reviewRedisFacade.getLatestViewCount(publishedReview.getId(), publishedReview.getViewCount());
			assertThat(viewCountAfterRead).isEqualTo(viewCountBeforeRead);
		}
	}

	@Test
	@DisplayName("Fail - 조회하려는 후기 id 가 없으면 실패하고 404 응답한다")
	void getReviewFailByNotFound() throws Exception {
		// given
		Long wrongReviewId = -1L;

		// when & then
		mockMvc.perform(get("/api/v1/reviews/{reviewId}", wrongReviewId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andDo(print());
	}

	@Test
	@DisplayName("Success - 동시에 50명이 조회했을 때 조회 수가 50개 증가한다")
	void increaseViewCountInMultiThreads() throws InterruptedException {
		// given
		ExecutorService executorService = Executors.newFixedThreadPool(50);
		CountDownLatch latch = new CountDownLatch(50);

		Thread.sleep(500);

		// when
		for (int i = 0; i < 50; i++) {
			executorService.submit(() -> {
				try {
					mockMvc.perform(get("/api/v1/reviews/{reviewId}", publishedReview.getId())
							.accept(MediaType.APPLICATION_JSON));
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		// then
		int latestViewCount = reviewRedisFacade.getLatestViewCount(publishedReview.getId(), publishedReview.getViewCount());
		assertThat(latestViewCount).isEqualTo(50);
	}
}