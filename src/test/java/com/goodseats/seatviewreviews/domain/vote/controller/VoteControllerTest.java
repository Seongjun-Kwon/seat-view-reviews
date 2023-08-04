package com.goodseats.seatviewreviews.domain.vote.controller;

import static com.goodseats.seatviewreviews.domain.vote.model.vo.VoteChoice.*;
import static com.goodseats.seatviewreviews.domain.vote.model.vo.VoteType.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodseats.seatviewreviews.common.TestUtils;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatGradeRepository;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatRepository;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatSectionRepository;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.stadium.repository.StadiumRepository;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.VoteCreateRequest;
import com.goodseats.seatviewreviews.domain.vote.model.entity.Vote;
import com.goodseats.seatviewreviews.domain.vote.repository.VoteRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class VoteControllerTest {

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
	private VoteRepository voteRepository;

	private Member voter;
	private Member alreadyVoter;
	private Stadium stadium;
	private SeatGrade seatGrade;
	private SeatSection seatSection;
	private Seat seat;
	private Review tempReview;
	private Review publishedReview;
	private Vote vote;

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

		memberRepository.save(voter);
		memberRepository.save(alreadyVoter);
		stadiumRepository.save(stadium);
		seatGradeRepository.save(seatGrade);
		seatSectionRepository.save(seatSection);
		seatRepository.save(seat);
		reviewRepository.save(tempReview);
		reviewRepository.save(publishedReview);

		vote = new Vote(REVIEW, publishedReview.getId(), LIKE, alreadyVoter);
		voteRepository.save(vote);
	}

	@AfterEach
	void clear() {
		memberRepository.delete(voter);
		memberRepository.delete(alreadyVoter);
		stadiumRepository.delete(stadium);
		seatGradeRepository.delete(seatGrade);
		seatSectionRepository.delete(seatSection);
		seatRepository.delete(seat);
		reviewRepository.deleteAll();
		voteRepository.delete(vote);
	}

	@Test
	@DisplayName("Success - 투표 생성에 성공하고 204 응답한다")
	void createVoteSuccess() throws Exception {
		// given
		MockHttpSession session = TestUtils.getLoginSession(voter, MemberAuthority.USER);
		Long memberId = voter.getId();
		Long referenceId = publishedReview.getId();
		VoteCreateRequest voteCreateRequest = new VoteCreateRequest(memberId, REVIEW, referenceId, LIKE);

		// when & then
		mockMvc.perform(post("/api/v1/votes")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(voteCreateRequest)))
				.andExpect(status().isNoContent())
				.andDo(document("투표 생성 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
						requestFields(
								fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("투표하는 회원의 id"),
								fieldWithPath("voteType").type(JsonFieldType.STRING).description("투표 대상 엔티티의 타입"),
								fieldWithPath("referenceId").type(JsonFieldType.NUMBER).description("투표 대상 엔티티의 id"),
								fieldWithPath("voteChoice").type(JsonFieldType.STRING).description("선택한 투표(좋아요, 싫어요)")
						)
				));
	}

	@Nested
	@DisplayName("createVoteFail")
	class CreateVoteFail {

		@Test
		@DisplayName("Fail - 비로그인 사용자가 투표하면 투표 생성에 실패하고 401 응답한다")
		void createVoteFailByNotMember() throws Exception {
			// given
			Long memberId = voter.getId();
			Long referenceId = publishedReview.getId();

			VoteCreateRequest voteCreateRequest = new VoteCreateRequest(memberId, REVIEW, referenceId, LIKE);

			// when & then
			mockMvc.perform(post("/api/v1/votes")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(voteCreateRequest)))
					.andExpect(status().isUnauthorized())
					.andDo(document("투표 생성 실패 - 비로그인 사용자가 시도한 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestFields(
									fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("투표하는 회원의 id"),
									fieldWithPath("voteType").type(JsonFieldType.STRING).description("투표 대상 엔티티의 타입"),
									fieldWithPath("referenceId").type(JsonFieldType.NUMBER).description("투표 대상 엔티티의 id"),
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
		@DisplayName("Fail - 투표하는 회원이 없는 회원이면 투표 생성에 실패하고 404 응답한다")
		void createVoteFailByNotFoundMember() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(voter, MemberAuthority.USER);
			Long memberId = 0L;
			Long referenceId = publishedReview.getId();

			VoteCreateRequest voteCreateRequest = new VoteCreateRequest(memberId, REVIEW, referenceId, LIKE);

			// when & then
			mockMvc.perform(post("/api/v1/votes")
							.session(session)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(voteCreateRequest)))
					.andExpect(status().isNotFound())
					.andDo(document("투표 생성 실패 - 투표자 id 없는 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestFields(
									fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("투표하는 회원의 id"),
									fieldWithPath("voteType").type(JsonFieldType.STRING).description("투표 대상 엔티티의 타입"),
									fieldWithPath("referenceId").type(JsonFieldType.NUMBER).description("투표 대상 엔티티의 id"),
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
		@DisplayName("Fail - 투표하는 엔티티(후기, 댓글)가 없으면 투표 생성에 실패하고 404 응답한다")
		void createVoteFailByNotFoundVoteType() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(voter, MemberAuthority.USER);
			Long memberId = voter.getId();
			Long referenceId = 0L;

			VoteCreateRequest voteCreateRequest = new VoteCreateRequest(memberId, REVIEW, referenceId, LIKE);

			// when & then
			mockMvc.perform(post("/api/v1/votes")
							.session(session)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(voteCreateRequest)))
					.andExpect(status().isNotFound())
					.andDo(document("투표 생성 실패 - 투표하는 엔티티의 id 없는 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestFields(
									fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("투표하는 회원의 id"),
									fieldWithPath("voteType").type(JsonFieldType.STRING).description("투표 대상 엔티티의 타입"),
									fieldWithPath("referenceId").type(JsonFieldType.NUMBER).description("투표 대상 엔티티의 id"),
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
		@DisplayName("Fail - 이미 투표했으면 투표 생성에 실패하고 409 응답한다")
		void createVoteFailByAlreadyVote() throws Exception {
			// given
			MockHttpSession session = TestUtils.getLoginSession(alreadyVoter, MemberAuthority.USER);
			Long memberId = alreadyVoter.getId();
			Long referenceId = publishedReview.getId();

			VoteCreateRequest voteCreateRequest = new VoteCreateRequest(memberId, REVIEW, referenceId, LIKE);

			// when & then
			mockMvc.perform(post("/api/v1/votes")
							.session(session)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(voteCreateRequest)))
					.andExpect(status().isConflict())
					.andDo(document("투표 생성 실패 - 이미 투표한 경우",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(headerWithName("Content-type").description("요청 타입 정보")),
							requestFields(
									fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("투표하는 회원의 id"),
									fieldWithPath("voteType").type(JsonFieldType.STRING).description("투표 대상 엔티티의 타입"),
									fieldWithPath("referenceId").type(JsonFieldType.NUMBER).description("투표 대상 엔티티의 id"),
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
}