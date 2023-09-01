package com.goodseats.seatviewreviews.domain.review.controller;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodseats.seatviewreviews.common.TestUtils;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.mapper.ReviewMapper;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewPublishRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.TempReviewCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewsResponse;
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
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
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
		publishedReview = new Review(writer, seat);
		publishedReview.publish("테스트 제목", "테스트 내용", 5);

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
		memberRepository.deleteAllInBatch();
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
				.andExpect(status().isCreated())
				.andDo(print())
				.andDo(document("후기 임시 생성 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(
								headerWithName("Content-type").description("요청 타입 정보"),
								headerWithName("Accept").description("가능한 응답 타임 정보")
						),
						requestFields(fieldWithPath("seatId").type(JsonFieldType.NUMBER).description("후기 작성하는 좌석 id")),
						responseHeaders(headerWithName("Location").description("생성된 후기에 접근 가능한 url"))));
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
				.andExpect(status().isNotFound())
				.andDo(print())
				.andDo(document("후기 임시 생성 실패 - 좌석 id 없는 경우",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(
								headerWithName("Content-type").description("요청 타입 정보"),
								headerWithName("Accept").description("가능한 응답 타임 정보")
						),
						requestFields(fieldWithPath("seatId").type(JsonFieldType.NUMBER).description("후기 작성하는 좌석 id")),
						responseFields(
								fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
								fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
								fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
								fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
								fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
								fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
						)));
	}

	@Test
	@DisplayName("Success - 후기 발행에 성공하고 204 응답한다")
	void publishReviewSuccess() throws Exception {
		// given
		MockHttpSession session = TestUtils.getLoginSession(writer, MemberAuthority.USER);
		ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/v1/reviews/{reviewId}", tempReview.getId())
						.session(session)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("title", reviewPublishRequest.title())
						.param("content", reviewPublishRequest.content())
						.param("score", String.valueOf(reviewPublishRequest.score())))
				.andExpect(status().isNoContent())
				.andDo(print())
				.andDo(document("후기 발행 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
						pathParameters(parameterWithName("reviewId").description("발행하려는 후기 id")),
						requestParameters(
								parameterWithName("title").description("후기 제목"),
								parameterWithName("content").description("후기 내용"),
								parameterWithName("score").description("좌석에 준 평점")
						)));
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
			mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/v1/reviews/{reviewId}", wrongReviewId)
							.session(session)
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.param("title", reviewPublishRequest.title())
							.param("content", reviewPublishRequest.content())
							.param("score", String.valueOf(reviewPublishRequest.score())))
					.andExpect(status().isNotFound())
					.andDo(print())
					.andDo(document("후기 발행 실패 - 임시 후기 id 없는 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							pathParameters(parameterWithName("reviewId").description("발행하려는 후기 id")),
							requestParameters(
									parameterWithName("title").description("후기 제목"),
									parameterWithName("content").description("후기 내용"),
									parameterWithName("score").description("좌석에 준 평점")
							),
							responseFields(
									fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
									fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
									fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
									fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
									fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
									fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
							)));
		}

		@Test
		@DisplayName("Fail - 발행하는 회원이 임시 후기를 작성한 회원이 아니면 실패하고 401 응답한다")
		void publishReviewFailByNotTempReviewWriter() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(notWriter, MemberAuthority.USER);
			ReviewPublishRequest reviewPublishRequest = new ReviewPublishRequest("테스트 제목", "테스트 내용", 5);

			// when & then
			mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/v1/reviews/{reviewId}", tempReview.getId())
							.session(session)
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.param("title", reviewPublishRequest.title())
							.param("content", reviewPublishRequest.content())
							.param("score", String.valueOf(reviewPublishRequest.score())))
					.andExpect(status().isUnauthorized())
					.andDo(print())
					.andDo(document("후기 발행 실패 - 임시 후기 작성자가 아닌 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							pathParameters(parameterWithName("reviewId").description("발행하려는 후기 id")),
							requestParameters(
									parameterWithName("title").description("후기 제목"),
									parameterWithName("content").description("후기 내용"),
									parameterWithName("score").description("좌석에 준 평점")
							),
							responseFields(
									fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
									fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
									fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
									fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
									fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
									fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
							)));
		}
	}

	@Nested
	@DisplayName("getReviewSuccess")
	class getReviewSuccess {
		@Test
		@DisplayName("Success - 처음 조회하는 후기의 상세 조회에 성공하고 200 응답한다")
		void getReviewSuccessWhenFirstRead() throws Exception {
			// given & when & then
			mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/reviews/{reviewId}", publishedReview.getId())
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("title").value(publishedReview.getTitle()))
					.andExpect(jsonPath("content").value(publishedReview.getContent()))
					.andExpect(jsonPath("score").value(publishedReview.getScore()))
					.andExpect(jsonPath("viewCount").value(publishedReview.getViewCount()))
					.andExpect(jsonPath("writer").value(publishedReview.getMember().getNickname()))
					.andDo(print())
					.andDo(document("후기 상세 조회 성공",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Accept").description("가능한 응답 타입 정보")),
							pathParameters(parameterWithName("reviewId").description("조회하려는 후기 id")),
							responseFields(
									fieldWithPath("title").type(JsonFieldType.STRING).description("후기 제목"),
									fieldWithPath("content").type(JsonFieldType.STRING).description("후기 내용"),
									fieldWithPath("score").type(JsonFieldType.NUMBER).description("좌석에 준 평점"),
									fieldWithPath("viewCount").type(JsonFieldType.NUMBER).description("후기 조회 수"),
									fieldWithPath("writer").type(JsonFieldType.STRING).description("후기 작성자")
							)));
		}

		@Test
		@DisplayName("Success - 사용자가 처음 조회한 후기의 조회 수가 1 오른다")
		void getReviewSuccessWhenAlreadyRead() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(writer, MemberAuthority.USER);
			int viewCountBeforeRead
					= reviewRedisFacade.getLatestViewCount(publishedReview.getId(), publishedReview.getViewCount());

			// when & then
			mockMvc.perform(get("/api/v1/reviews/{reviewId}", publishedReview.getId())
							.accept(MediaType.APPLICATION_JSON)
							.session(session))
					.andExpect(status().isOk())
					.andExpect(jsonPath("title").value(publishedReview.getTitle()))
					.andExpect(jsonPath("content").value(publishedReview.getContent()))
					.andExpect(jsonPath("score").value(publishedReview.getScore()))
					.andExpect(jsonPath("viewCount").value(publishedReview.getViewCount()))
					.andExpect(jsonPath("writer").value(publishedReview.getMember().getNickname()))
					.andDo(print());

			int viewCountAfterRead
					= reviewRedisFacade.getLatestViewCount(publishedReview.getId(), publishedReview.getViewCount());
			assertThat(viewCountAfterRead).isEqualTo(viewCountBeforeRead + 1);
		}

		@Test
		@DisplayName("Success - 사용자가 이미 조회 한 후기를 조회 시 조회 수가 늘어나지 않는다")
		void notIncreaseViewCountWhenAlreadyRead() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(writer, MemberAuthority.USER);

			mockMvc.perform(get("/api/v1/reviews/{reviewId}", publishedReview.getId())
							.accept(MediaType.APPLICATION_JSON)
							.session(session))
					.andDo(print());

			int viewCountBeforeRead
					= reviewRedisFacade.getLatestViewCount(publishedReview.getId(), publishedReview.getViewCount());

			// when
			mockMvc.perform(get("/api/v1/reviews/{reviewId}", publishedReview.getId())
							.accept(MediaType.APPLICATION_JSON)
							.session(session))
					.andExpect(status().isOk())
					.andDo(print());

			// then
			int viewCountAfterRead
					= reviewRedisFacade.getLatestViewCount(publishedReview.getId(), publishedReview.getViewCount());
			assertThat(viewCountAfterRead).isEqualTo(viewCountBeforeRead);
		}
	}

	@Test
	@DisplayName("Fail - 조회하려는 후기 id 가 없으면 상세 조회에 실패하고 404 응답한다")
	void getReviewFailByNotFound() throws Exception {
		// given
		Long wrongReviewId = -1L;

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/reviews/{reviewId}", wrongReviewId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andDo(print())
				.andDo(document("후기 상세 조회 실패 - 후기 id 없는 경우",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Accept").description("가능한 응답 타입 정보")),
						pathParameters(parameterWithName("reviewId").description("조회하려는 후기 id")),
						responseFields(
								fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
								fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
								fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
								fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
								fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
								fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
						)));
	}

	@Test
	@DisplayName("Success - 동시에 100명이 조회했을 때 조회 수가 100개 증가한다")
	void increaseViewCountInMultiThreads() throws InterruptedException {
		// given
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		CountDownLatch latch = new CountDownLatch(100);

		Thread.sleep(500);

		// when
		for (int i = 0; i < 100; i++) {
			Member member = new Member("test" + i + "@test.com", "test", "test" + i);
			memberRepository.saveAndFlush(member);
			MockHttpSession session= TestUtils.getLoginSession(member, member.getMemberAuthority());

			executorService.submit(() -> {
				try {
					mockMvc.perform(get("/api/v1/reviews/{reviewId}", publishedReview.getId())
							.accept(MediaType.APPLICATION_JSON)
							.session(session));
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
		assertThat(latestViewCount).isEqualTo(100);
	}

	@Test
	@DisplayName("Success - 특정 좌석의 후기 목록 조회에 성공하고 200 응답한다")
	void getReviewsSuccess() throws Exception {
		// given
		Review tempReview1 = new Review(writer, seat);
		Review tempReview2 = new Review(writer, seat);
		tempReview1.publish("테스트 제목1", "테스트 내용1", 5);
		tempReview2.publish("테스트 제목2", "테스트 내용2", 5);
		reviewRepository.saveAndFlush(tempReview1);
		reviewRepository.saveAndFlush(tempReview2);

		Page<Review> reviewPage = reviewRepository.findAllWithFetchMemberBySeatIdAndPublishedTrue(
				seat.getId(), PageRequest.of(0, 1)
		);
		ReviewsResponse reviewsResponse = ReviewMapper.toReviewsResponse(reviewPage);

		// when & then
		mockMvc.perform(get("/api/v1/reviews")
						.queryParam("seatId", String.valueOf(seat.getId()))
						.queryParam("page", "0")
						.queryParam("size", "1")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(objectMapper.writeValueAsString(reviewsResponse)))
				.andDo(print())
				.andDo(document("후기 목록 조회 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Accept").description("가능한 응답 타입 정보")),
						requestParameters(
								parameterWithName("seatId").description("조회하려는 좌석 id"),
								parameterWithName("page").description("조회하려는 페이지 번호"),
								parameterWithName("size").description("한 페이지의 후기 수")
						),
						responseFields(
								fieldWithPath("reviews").type(JsonFieldType.ARRAY).description("후기 목록"),
								fieldWithPath("reviews[].reviewId").type(JsonFieldType.NUMBER).description("후기 목록"),
								fieldWithPath("reviews[].title").type(JsonFieldType.STRING).description("후기 제목"),
								fieldWithPath("reviews[].score").type(JsonFieldType.NUMBER).description("좌석에 준 평점"),
								fieldWithPath("reviews[].viewCount").type(JsonFieldType.NUMBER).description("후기 조회 수"),
								fieldWithPath("reviews[].writer").type(JsonFieldType.STRING).description("후기 작성자")
						)));
	}

	@Test
	@DisplayName("Fail - 후기 목록을 조회하려는 좌석이 존재하지 않는 좌석이면 실패하고 404 응답한다")
	void getReviewsFailByNotFoundSeat() throws Exception {
		// given
		Long wrongSeatId = 0L;

		// when & then
		mockMvc.perform(get("/api/v1/reviews")
						.queryParam("seatId", String.valueOf(wrongSeatId))
						.queryParam("page", "0")
						.queryParam("size", "1")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andDo(print())
				.andDo(document("후기 목록 조회 실패 - 좌석 id 없는 경우",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Accept").description("가능한 응답 타입 정보")),
						requestParameters(
								parameterWithName("seatId").description("조회하려는 좌석 id"),
								parameterWithName("page").description("조회하려는 페이지 번호"),
								parameterWithName("size").description("한 페이지의 후기 수")
						),
						responseFields(
								fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
								fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
								fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
								fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
								fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
								fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
						)));
	}
}