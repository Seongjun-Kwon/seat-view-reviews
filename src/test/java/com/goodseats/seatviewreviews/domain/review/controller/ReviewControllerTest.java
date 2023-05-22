package com.goodseats.seatviewreviews.domain.review.controller;

import static com.goodseats.seatviewreviews.common.security.SessionConstant.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.ReviewPublishRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.TempReviewCreateRequest;
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

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

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

	@Test
	@DisplayName("Success - 후기 임시 생성에 성공하고 200 응답한다")
	void createTempReviewSuccess() throws Exception {
		// given
		Member member = new Member("test@test.com", "test", "test");
		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		SeatSection seatSection = new SeatSection("110", stadium, seatGrade);
		Seat seat = new Seat("1", seatGrade, seatSection);

		memberRepository.save(member);
		stadiumRepository.save(stadium);
		seatGradeRepository.save(seatGrade);
		seatSectionRepository.save(seatSection);
		seatRepository.save(seat);

		AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), MemberAuthority.USER);
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(LOGIN_MEMBER_INFO, authenticationDTO);

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
		Long wrongSeatId = 0L;
		Member member = new Member("test@test.com", "test", "test");

		memberRepository.save(member);

		AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), MemberAuthority.USER);
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(LOGIN_MEMBER_INFO, authenticationDTO);

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
		Member member = new Member("test@test.com", "test", "test");
		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		SeatSection seatSection = new SeatSection("110", stadium, seatGrade);
		Seat seat = new Seat("1", seatGrade, seatSection);
		Review tempReview = new Review(member, seat);

		memberRepository.save(member);
		stadiumRepository.save(stadium);
		seatGradeRepository.save(seatGrade);
		seatSectionRepository.save(seatSection);
		seatRepository.save(seat);
		reviewRepository.save(tempReview);

		AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), MemberAuthority.USER);
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(LOGIN_MEMBER_INFO, authenticationDTO);

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
			Long wrongReviewId = 1L;
			Member member = new Member("test@test.com", "test", "test");

			memberRepository.save(member);

			AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), MemberAuthority.USER);
			MockHttpSession session = new MockHttpSession();
			session.setAttribute(LOGIN_MEMBER_INFO, authenticationDTO);

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
			Member writerMember = new Member("writer@test.com", "test", "writer");
			Member notWriterMember = new Member("notWriter@test.com", "test", "notWriter");
			Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
			SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
			SeatSection seatSection = new SeatSection("110", stadium, seatGrade);
			Seat seat = new Seat("1", seatGrade, seatSection);
			Review tempReview = new Review(writerMember, seat);

			memberRepository.save(writerMember);
			memberRepository.save(notWriterMember);
			stadiumRepository.save(stadium);
			seatGradeRepository.save(seatGrade);
			seatSectionRepository.save(seatSection);
			seatRepository.save(seat);
			reviewRepository.save(tempReview);

			AuthenticationDTO authenticationDTO = new AuthenticationDTO(notWriterMember.getId(), MemberAuthority.USER);
			MockHttpSession session = new MockHttpSession();
			session.setAttribute(LOGIN_MEMBER_INFO, authenticationDTO);

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
}