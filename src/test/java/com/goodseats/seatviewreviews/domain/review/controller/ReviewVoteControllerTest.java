package com.goodseats.seatviewreviews.domain.review.controller;

import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;
import static com.goodseats.seatviewreviews.domain.review.model.vo.VoteChoice.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodseats.seatviewreviews.common.TestUtils;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewVoteCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.model.entity.ReviewVote;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewVoteRepository;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.stadium.repository.SeatGradeRepository;
import com.goodseats.seatviewreviews.domain.stadium.repository.SeatRepository;
import com.goodseats.seatviewreviews.domain.stadium.repository.SeatSectionRepository;
import com.goodseats.seatviewreviews.domain.stadium.repository.StadiumRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class ReviewVoteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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

	@Autowired
	private ReviewVoteRepository reviewVoteRepository;

	private Member voter;
	private Member alreadyVoter;
	private List<Member> members = new ArrayList<>();
	private Stadium stadium;
	private SeatGrade seatGrade;
	private SeatSection seatSection;
	private Seat seat;
	private Review tempReview;
	private Review publishedReview;
	private ReviewVote reviewVote;

	@BeforeEach
	void setUp() {
		voter = new Member("voter@test.com", "test", "voter");
		alreadyVoter = new Member("alreadyVoter@test.com", "test", "already");
		stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		seatSection = new SeatSection("110", stadium, seatGrade);
		seat = new Seat("1", seatGrade, seatSection);
		tempReview = new Review(voter, seat);
		publishedReview = new Review(voter, seat);
		publishedReview.publish("테스트 제목", "테스트 내용", 5);

		for (int i = 0; i < 100; i++) {
			members.add(new Member(i + "@test.com", "test", String.valueOf(i)));
		}

		memberRepository.save(voter);
		memberRepository.save(alreadyVoter);
		memberRepository.saveAll(members);
		stadiumRepository.save(stadium);
		seatGradeRepository.save(seatGrade);
		seatSectionRepository.save(seatSection);
		seatRepository.save(seat);
		reviewRepository.save(tempReview);
		reviewRepository.save(publishedReview);

		reviewVote = new ReviewVote(LIKE, alreadyVoter, publishedReview);
		publishedReview.updateVoteCount(1, LIKE);
		reviewVoteRepository.save(reviewVote);
	}

	@AfterEach
	void clear() {
		memberRepository.delete(voter);
		memberRepository.delete(alreadyVoter);
		memberRepository.deleteAllInBatch(members);
		stadiumRepository.delete(stadium);
		seatGradeRepository.delete(seatGrade);
		seatSectionRepository.delete(seatSection);
		seatRepository.delete(seat);
		reviewRepository.deleteAll();
		reviewVoteRepository.deleteAll();
	}

	@Test
	@DisplayName("Success - 후기 투표 생성에 성공하고 201 응답한다")
	void createVoteSuccess() throws Exception {
		// given
		MockHttpSession session = TestUtils.getLoginSession(voter, USER);
		ReviewVoteCreateRequest reviewVoteCreateRequest = new ReviewVoteCreateRequest(publishedReview.getId(), LIKE);

		// when & then
		mockMvc.perform(post("/api/v1/reviewvotes")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reviewVoteCreateRequest)))
				.andExpect(status().isCreated())
				.andDo(document("후기 투표 생성 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
						requestFields(
								fieldWithPath("reviewId").type(JsonFieldType.NUMBER).description("연관된 후기의 id"),
								fieldWithPath("voteChoice").type(JsonFieldType.STRING).description("선택한 투표(좋아요, 싫어요)")
						),
						responseHeaders(headerWithName("Location").description("생성된 후기 투표에 접근 가능한 url"))));
	}

	@Nested
	@DisplayName("createVoteFail")
	class CreateReviewVoteFail {

		@Test
		@DisplayName("Fail - 비로그인 사용자가 투표하면 후기 투표 생성에 실패하고 401 응답한다")
		void createVoteFailByNotMember() throws Exception {
			// given
			ReviewVoteCreateRequest reviewVoteCreateRequest = new ReviewVoteCreateRequest(publishedReview.getId(), LIKE);

			// when & then
			mockMvc.perform(post("/api/v1/reviewvotes")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(reviewVoteCreateRequest)))
					.andExpect(status().isUnauthorized())
					.andDo(document("후기 투표 생성 실패 - 비로그인 사용자가 시도한 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestFields(
									fieldWithPath("reviewId").type(JsonFieldType.NUMBER).description("연관된 후기의 id"),
									fieldWithPath("voteChoice").type(JsonFieldType.STRING).description("선택한 투표(좋아요, 싫어요)")
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
		@DisplayName("Fail - 투표하는 회원이 없는 회원이면 후기 투표 생성에 실패하고 404 응답한다")
		void createVoteFailByNotFoundMember() throws Exception {
			// given
			Member deletedMember = new Member("deleted@test.com", "test", "deleted");
			ReflectionTestUtils.setField(deletedMember, "id", 0L);
			MockHttpSession session = TestUtils.getLoginSession(deletedMember, USER);
			ReviewVoteCreateRequest reviewVoteCreateRequest = new ReviewVoteCreateRequest(publishedReview.getId(), LIKE);

			// when & then
			mockMvc.perform(post("/api/v1/reviewvotes")
							.session(session)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(reviewVoteCreateRequest)))
					.andExpect(status().isNotFound())
					.andDo(document("후기 투표 생성 실패 - 투표자 id 없는 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestFields(
									fieldWithPath("reviewId").type(JsonFieldType.NUMBER).description("연관된 후기의 id"),
									fieldWithPath("voteChoice").type(JsonFieldType.STRING).description("선택한 투표(좋아요, 싫어요)")
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
		@DisplayName("Fail - 투표하는 엔티티(후기, 댓글)가 없으면 후기 투표 생성에 실패하고 404 응답한다")
		void createVoteFailByNotFoundVoteType() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(voter, USER);
			ReviewVoteCreateRequest reviewVoteCreateRequest
					= new ReviewVoteCreateRequest(0L, LIKE);

			// when & then
			mockMvc.perform(post("/api/v1/reviewvotes")
							.session(session)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(reviewVoteCreateRequest)))
					.andExpect(status().isNotFound())
					.andDo(document("후기 투표 생성 실패 - 투표하는 엔티티의 id 없는 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestFields(
									fieldWithPath("reviewId").type(JsonFieldType.NUMBER).description("연관된 후기의 id"),
									fieldWithPath("voteChoice").type(JsonFieldType.STRING).description("선택한 투표(좋아요, 싫어요)")
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
		@DisplayName("Fail - 이미 투표했으면 후기 투표 생성에 실패하고 409 응답한다")
		void createVoteFailByAlreadyVote() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(alreadyVoter, USER);
			ReviewVoteCreateRequest reviewVoteCreateRequest
					= new ReviewVoteCreateRequest(publishedReview.getId(), LIKE);

			// when & then
			mockMvc.perform(post("/api/v1/reviewvotes")
							.session(session)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(reviewVoteCreateRequest)))
					.andExpect(status().isConflict())
					.andDo(document("후기 투표 생성 실패 - 이미 투표한 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestFields(
									fieldWithPath("reviewId").type(JsonFieldType.NUMBER).description("연관된 후기의 id"),
									fieldWithPath("voteChoice").type(JsonFieldType.STRING).description("선택한 투표(좋아요, 싫어요)")
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

	@Test
	@DisplayName("Success - 후기 투표 삭제에 성공하고 204 응답한다")
	void deleteVoteSuccess() throws Exception {
		// given
		MockHttpSession session = TestUtils.getLoginSession(alreadyVoter, USER);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.delete(
								"/api/v1/reviewvotes/{reviewVoteId}", reviewVote.getId()
						)
						.session(session))
				.andExpect(status().isNoContent())
				.andDo(print())
				.andDo(document("후기 투표 삭제 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						pathParameters(parameterWithName("reviewVoteId").description("삭제하려는 후기 투표 id"))
				));
	}

	@Nested
	@DisplayName("deleteVoteFail")
	class DeleteVoteFail {

		@Test
		@DisplayName("Fail - 삭제하려는 후기 투표가 존재하지 않으면 후기 투표 삭제에 실패하고 404 응답한다")
		void deleteVoteFailByNotFoundReviewVote() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(alreadyVoter, USER);
			Long wrongReviewVoteId = 0L;

			// when & then
			mockMvc.perform(RestDocumentationRequestBuilders.delete(
									"/api/v1/reviewvotes/{reviewVoteId}", wrongReviewVoteId
							)
							.session(session))
					.andExpect(status().isNotFound())
					.andDo(print())
					.andDo(document("후기 투표 삭제 실패 - 후기 투표 id 없는 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							pathParameters(parameterWithName("reviewVoteId").description("삭제하려는 후기 투표 id")),
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
		@DisplayName("Fail - 삭제하려는 후기 투표의 투표자가 아니면 후기 투표 삭제에 실패하고 401 응답한다")
		void deleteVoteFailByUnAuthorized() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(voter, USER);

			// when & then
			mockMvc.perform(RestDocumentationRequestBuilders.delete(
									"/api/v1/reviewvotes/{reviewVoteId}", reviewVote.getId()
							)
							.session(session))
					.andExpect(status().isUnauthorized())
					.andDo(print())
					.andDo(document("후기 투표 삭제 실패 - 후기 투표자가 아닌 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							pathParameters(parameterWithName("reviewVoteId").description("삭제하려는 후기 투표 id")),
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

	@Test
	@DisplayName("Success - 후기 투표 정보 조회에 성공하고 200 응답한다")
	void getVotesSuccessWhenLogin() throws Exception {
		// given
		MockHttpSession session = TestUtils.getLoginSession(voter, USER);

		// when & then
		mockMvc.perform(get("/api/v1/reviewvotes")
						.param("reviewId", publishedReview.getId().toString())
						.session(session))
				.andExpect(status().isOk())
				.andDo(print())
				.andDo(document("후기 투표 조회 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestParameters(parameterWithName("reviewId").description("연관된 후기 id")),
						responseFields(
								fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 수"),
								fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("싫어요 수"),
								fieldWithPath("clickLike").type(JsonFieldType.BOOLEAN).description("좋아요 등록 여부"),
								fieldWithPath("clickDislike").type(JsonFieldType.BOOLEAN).description("싫어요 등록 여부")
						)));
	}

	@Test
	@DisplayName("Fail - 연관된 후기가 없으면 후기 투표 조회에 실패하고 404 응답한다")
	void getVotesFailByNotFoundReview() throws Exception {
		// given
		MockHttpSession session = TestUtils.getLoginSession(voter, USER);
		Long wrongReviewId = 0L;

		// when & then
		mockMvc.perform(get("/api/v1/reviewvotes")
						.param("reviewId", wrongReviewId.toString())
						.session(session))
				.andExpect(status().isNotFound())
				.andDo(print())
				.andDo(document("후기 투표 조회 실패 - 연관된 후기가 없는 경우",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestParameters(parameterWithName("reviewId").description("연관된 후기 id")),
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
	@DisplayName("Success - 동시에 100명이 좋아요를 누를 때 후기 좋아요 수가 100 증가한다")
	void increaseLikeCountInMultiThreads() throws InterruptedException {
		// given
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		CountDownLatch latch = new CountDownLatch(100);
		ReviewVoteCreateRequest reviewVoteCreateRequest = new ReviewVoteCreateRequest(publishedReview.getId(), LIKE);

		Thread.sleep(500);

		// when
		for (int i = 0; i < 100; i++) {
			Member member = memberRepository.findById(members.get(i).getId()).get();
			MockHttpSession session = TestUtils.getLoginSession(member, member.getMemberAuthority());

			executorService.submit(() -> {
				try {
					mockMvc.perform(post("/api/v1/reviewvotes")
									.accept(MediaType.APPLICATION_JSON)
									.contentType(MediaType.APPLICATION_JSON)
									.session(session)
									.content(objectMapper.writeValueAsString(reviewVoteCreateRequest)))
							.andExpect(status().isCreated());
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		// then
		Review review = reviewRepository.findById(publishedReview.getId()).get();
		assertThat(review.getLikeCount()).isEqualTo(publishedReview.getLikeCount() + 100);
	}
}