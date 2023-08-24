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

import com.goodseats.seatviewreviews.common.error.exception.AuthenticationException;
import com.goodseats.seatviewreviews.common.error.exception.DuplicatedException;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;
import com.goodseats.seatviewreviews.domain.member.repository.MemberRepository;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.ReviewVoteCreateRequest;
import com.goodseats.seatviewreviews.domain.vote.model.dto.request.ReviewVotesGetRequest;
import com.goodseats.seatviewreviews.domain.vote.model.dto.response.ReviewVotesResponse;
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
	private ReviewVote reviewVote;

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
		reviewVote = new ReviewVote(LIKE, member, publishedReview);

		ReflectionTestUtils.setField(member, "id", 1L);
		ReflectionTestUtils.setField(stadium, "id", 1L);
		ReflectionTestUtils.setField(seatGrade, "id", 1L);
		ReflectionTestUtils.setField(seatSection, "id", 1L);
		ReflectionTestUtils.setField(seat, "id", 1L);
		ReflectionTestUtils.setField(tempReview, "id", 1L);
		ReflectionTestUtils.setField(publishedReview, "id", 1L);
		ReflectionTestUtils.setField(reviewVote, "id", 1L);
	}

	@Test
	@DisplayName("Success - 후기 투표 생성에 성공한다")
	void createVoteSuccess() {
		// given
		Long memberId = member.getId();
		ReviewVoteCreateRequest reviewVoteCreateRequest = new ReviewVoteCreateRequest(publishedReview.getId(), LIKE);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(reviewRepository.findById(reviewVoteCreateRequest.reviewId())).thenReturn(Optional.of(publishedReview));
		when(reviewVoteRepository.existsByMemberAndReview(member, publishedReview)).thenReturn(false);
		when(reviewVoteRepository.save(any(ReviewVote.class))).thenReturn(reviewVote);
		when(reviewVoteRepository.getVoteCount(reviewVoteCreateRequest.reviewId(), reviewVoteCreateRequest.voteChoice()))
				.thenReturn(anyInt());

		// when
		reviewVoteService.createVote(reviewVoteCreateRequest, memberId);

		// then
		verify(memberRepository).findById(memberId);
		verify(reviewRepository).findById(reviewVoteCreateRequest.reviewId());
		verify(reviewVoteRepository).existsByMemberAndReview(member, publishedReview);
		verify(reviewVoteRepository).save(any(ReviewVote.class));
		verify(reviewVoteRepository).getVoteCount(reviewVoteCreateRequest.reviewId(), reviewVoteCreateRequest.voteChoice());
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
			ReviewVoteCreateRequest reviewVoteCreateRequest = new ReviewVoteCreateRequest(publishedReview.getId(), LIKE);

			when(memberRepository.findById(wrongMember.getId())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> reviewVoteService.createVote(reviewVoteCreateRequest, wrongMember.getId()))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(memberRepository).findById(wrongMember.getId());
		}

		@Test
		@DisplayName("Fail - 투표하는 엔티티(후기, 댓글)가 없으면 후기 투표 생성에 실패한다")
		void createVoteFailByNotFoundVoteType() {
			// given
			Long memberId = member.getId();
			Review wrongReview = new Review(member, seat);
			ReflectionTestUtils.setField(wrongReview, "id", -1L);
			ReviewVoteCreateRequest reviewVoteCreateRequest = new ReviewVoteCreateRequest(wrongReview.getId(), LIKE);

			when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
			when(reviewRepository.findById(reviewVoteCreateRequest.reviewId())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> reviewVoteService.createVote(reviewVoteCreateRequest, memberId))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(memberRepository).findById(memberId);
			verify(reviewRepository).findById(reviewVoteCreateRequest.reviewId());
		}

		@Test
		@DisplayName("Fail - 이미 투표했으면 후기 투표 생성에 실패한다")
		void createVoteFailByAlreadyVote() {
			// given
			Long memberId = member.getId();
			ReviewVoteCreateRequest reviewVoteCreateRequest = new ReviewVoteCreateRequest(publishedReview.getId(), LIKE);

			when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
			when(reviewRepository.findById(reviewVoteCreateRequest.reviewId())).thenReturn(Optional.of(publishedReview));
			when(reviewVoteRepository.existsByMemberAndReview(member, publishedReview)).thenReturn(true);

			// when & then
			assertThatThrownBy(() -> reviewVoteService.createVote(reviewVoteCreateRequest, memberId))
					.isExactlyInstanceOf(DuplicatedException.class)
					.hasMessage(ALREADY_VOTED.getMessage());

			verify(memberRepository).findById(memberId);
			verify(reviewRepository).findById(reviewVoteCreateRequest.reviewId());
			verify(reviewVoteRepository).existsByMemberAndReview(member, publishedReview);
		}
	}

	@Test
	@DisplayName("Success - 후기 투표 삭제에 성공한다")
	void deleteVoteSuccess() {
		// given
		when(reviewVoteRepository.findById(reviewVote.getId())).thenReturn(Optional.of(reviewVote));
		doNothing().when(reviewVoteRepository).delete(reviewVote);
		when(reviewVoteRepository.getVoteCount(reviewVote.getReview().getId(), reviewVote.getVoteChoice()))
				.thenReturn(anyInt());

		// when
		reviewVoteService.deleteVote(reviewVote.getId(), member.getId());

		// then
		verify(reviewVoteRepository).findById(reviewVote.getId());
		verify(reviewVoteRepository).delete(reviewVote);
		verify(reviewVoteRepository).getVoteCount(reviewVote.getReview().getId(), reviewVote.getVoteChoice());
	}

	@Nested
	@DisplayName("deleteVoteFail")
	class DeleteVoteFail {

		@Test
		@DisplayName("Fail - 삭제하려는 후기 투표가 존재하지 않으면 후기 투표 삭제에 실패한다")
		void deleteVoteFailByNotFoundReviewVote() {
			// given
			Long wrongReviewVoteId = 0L;
			when(reviewVoteRepository.findById(wrongReviewVoteId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> reviewVoteService.deleteVote(wrongReviewVoteId, member.getId()))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(reviewVoteRepository).findById(wrongReviewVoteId);
		}

		@Test
		@DisplayName("Fail - 삭제하려는 후기 투표의 투표자가 아니면 후기 투표 삭제에 실패한다")
		void deleteVoteFailByUnAuthorized() {
			// given
			Long wrongMemberId = 0L;
			when(reviewVoteRepository.findById(reviewVote.getId())).thenReturn(Optional.of(reviewVote));

			// when & then
			assertThatThrownBy(() -> reviewVoteService.deleteVote(reviewVote.getId(), wrongMemberId))
					.isExactlyInstanceOf(AuthenticationException.class)
					.hasMessage(UNAUTHORIZED.getMessage());
			verify(reviewVoteRepository).findById(reviewVote.getId());
		}
	}

	@Test
	@DisplayName("Success - 로그인한 경우 후기 투표 정보 조회 시 투표 수와 투표 여부 반환에 성공한다")
	void getVotesSuccessWhenLogin() {
		// given
		AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), MemberAuthority.USER);
		ReviewVotesGetRequest reviewVotesGetRequest
				= new ReviewVotesGetRequest(publishedReview.getId(), Optional.of(authenticationDTO));
		when(reviewRepository.findById(reviewVotesGetRequest.reviewId())).thenReturn(Optional.of(publishedReview));
		when(memberRepository.findById(reviewVotesGetRequest.authenticationDTO().get().memberId()))
				.thenReturn(Optional.of(member));
		when(reviewVoteRepository.findReviewVoteByMemberAndReview(any(Member.class), any(Review.class)))
				.thenReturn(Optional.of(reviewVote));

		// when
		ReviewVotesResponse reviewVotesResponse = reviewVoteService.getVotes(reviewVotesGetRequest);

		// then
		verify(reviewRepository).findById(reviewVotesGetRequest.reviewId());
		verify(memberRepository).findById(reviewVotesGetRequest.authenticationDTO().get().memberId());
		verify(reviewVoteRepository).findReviewVoteByMemberAndReview(any(Member.class), any(Review.class));
		assertThat(reviewVotesResponse.likeCount()).isEqualTo(publishedReview.getLikeCount());
		assertThat(reviewVotesResponse.dislikeCount()).isEqualTo(publishedReview.getLikeCount());
		assertThat(reviewVotesResponse.clickLike()).isEqualTo(reviewVote.isLike());
		assertThat(reviewVotesResponse.clickDislike()).isEqualTo(reviewVote.isDislike());
	}

	@Test
	@DisplayName("Success - 비로그인인 경우 후기 투표 정보 조회에 성공한다")
	void getVotesSuccessWhenNotLogin() {
		// given
		ReviewVotesGetRequest reviewVotesGetRequest
				= new ReviewVotesGetRequest(publishedReview.getId(), Optional.empty());
		when(reviewRepository.findById(reviewVotesGetRequest.reviewId())).thenReturn(Optional.of(publishedReview));

		// when
		ReviewVotesResponse reviewVotesResponse = reviewVoteService.getVotes(reviewVotesGetRequest);

		// then
		verify(reviewRepository).findById(reviewVotesGetRequest.reviewId());
		assertThat(reviewVotesResponse.likeCount()).isEqualTo(publishedReview.getLikeCount());
		assertThat(reviewVotesResponse.dislikeCount()).isEqualTo(publishedReview.getLikeCount());
		assertThat(reviewVotesResponse.clickLike()).isFalse();
		assertThat(reviewVotesResponse.clickDislike()).isFalse();
	}

	@Nested
	@DisplayName("getVotesFail")
	class GetVotesFail {
		@Test
		@DisplayName("Fail - 연관된 후기가 없으면 후기 투표 조회에 실패한다")
		void getVotesFailByNotFoundReview() {
			// given
			AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), MemberAuthority.USER);
			ReviewVotesGetRequest reviewVotesGetRequest
					= new ReviewVotesGetRequest(publishedReview.getId(), Optional.of(authenticationDTO));
			when(reviewRepository.findById(reviewVotesGetRequest.reviewId())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> reviewVoteService.getVotes(reviewVotesGetRequest))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(reviewRepository).findById(reviewVotesGetRequest.reviewId());
		}
	}
}