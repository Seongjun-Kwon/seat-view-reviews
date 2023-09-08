package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static com.goodseats.seatviewreviews.domain.review.model.vo.VoteChoice.*;
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
import com.goodseats.seatviewreviews.domain.review.model.dto.request.VoteCreateRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.request.VotesGetRequest;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.VotesResponse;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.model.entity.Vote;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.review.repository.VoteRepository;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

	@Mock
	private VoteRepository voteRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@InjectMocks
	private VoteService voteService;

	private Member member;
	private Stadium stadium;
	private SeatGrade seatGrade;
	private SeatSection seatSection;
	private Seat seat;
	private Review tempReview;
	private Review publishedReview;
	private Vote vote;

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
		vote = new Vote(LIKE, member, publishedReview);

		ReflectionTestUtils.setField(member, "id", 1L);
		ReflectionTestUtils.setField(stadium, "id", 1L);
		ReflectionTestUtils.setField(seatGrade, "id", 1L);
		ReflectionTestUtils.setField(seatSection, "id", 1L);
		ReflectionTestUtils.setField(seat, "id", 1L);
		ReflectionTestUtils.setField(tempReview, "id", 1L);
		ReflectionTestUtils.setField(publishedReview, "id", 1L);
		ReflectionTestUtils.setField(vote, "id", 1L);
	}

	@Test
	@DisplayName("Success - 후기 투표 생성에 성공한다")
	void createVoteSuccess() {
		// given
		Long memberId = member.getId();
		VoteCreateRequest voteCreateRequest = new VoteCreateRequest(publishedReview.getId(), LIKE);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(reviewRepository.findById(voteCreateRequest.reviewId())).thenReturn(Optional.of(publishedReview));
		when(voteRepository.existsByMemberAndReview(member, publishedReview)).thenReturn(false);
		when(voteRepository.save(any(Vote.class))).thenReturn(vote);
		when(voteRepository.getVoteCount(voteCreateRequest.reviewId(), voteCreateRequest.voteChoice()))
				.thenReturn(anyInt());

		// when
		voteService.createVote(voteCreateRequest, memberId);

		// then
		verify(memberRepository).findById(memberId);
		verify(reviewRepository).findById(voteCreateRequest.reviewId());
		verify(voteRepository).existsByMemberAndReview(member, publishedReview);
		verify(voteRepository).save(any(Vote.class));
		verify(voteRepository).getVoteCount(voteCreateRequest.reviewId(), voteCreateRequest.voteChoice());
	}

	@Nested
	@DisplayName("createVoteFail")
	class CreateVoteFail {

		@Test
		@DisplayName("Fail - 투표하는 회원이 없는 회원이면 후기 투표 생성에 실패한다")
		void createVoteFailByNotFoundMember() {
			// given
			Member wrongMember = new Member("test@test.com", "password", "wrongMember");
			ReflectionTestUtils.setField(wrongMember, "id", -1L);
			VoteCreateRequest voteCreateRequest = new VoteCreateRequest(publishedReview.getId(), LIKE);

			when(memberRepository.findById(wrongMember.getId())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> voteService.createVote(voteCreateRequest, wrongMember.getId()))
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
			VoteCreateRequest voteCreateRequest = new VoteCreateRequest(wrongReview.getId(), LIKE);

			when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
			when(reviewRepository.findById(voteCreateRequest.reviewId())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> voteService.createVote(voteCreateRequest, memberId))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(memberRepository).findById(memberId);
			verify(reviewRepository).findById(voteCreateRequest.reviewId());
		}

		@Test
		@DisplayName("Fail - 이미 투표했으면 후기 투표 생성에 실패한다")
		void createVoteFailByAlreadyVote() {
			// given
			Long memberId = member.getId();
			VoteCreateRequest voteCreateRequest = new VoteCreateRequest(publishedReview.getId(), LIKE);

			when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
			when(reviewRepository.findById(voteCreateRequest.reviewId())).thenReturn(Optional.of(publishedReview));
			when(voteRepository.existsByMemberAndReview(member, publishedReview)).thenReturn(true);

			// when & then
			assertThatThrownBy(() -> voteService.createVote(voteCreateRequest, memberId))
					.isExactlyInstanceOf(DuplicatedException.class)
					.hasMessage(ALREADY_VOTED.getMessage());

			verify(memberRepository).findById(memberId);
			verify(reviewRepository).findById(voteCreateRequest.reviewId());
			verify(voteRepository).existsByMemberAndReview(member, publishedReview);
		}
	}

	@Test
	@DisplayName("Success - 후기 투표 삭제에 성공한다")
	void deleteVoteSuccess() {
		// given
		when(voteRepository.findById(vote.getId())).thenReturn(Optional.of(vote));
		doNothing().when(voteRepository).delete(vote);
		when(voteRepository.getVoteCount(vote.getReview().getId(), vote.getVoteChoice()))
				.thenReturn(anyInt());

		// when
		voteService.deleteVote(vote.getId(), member.getId());

		// then
		verify(voteRepository).findById(vote.getId());
		verify(voteRepository).delete(vote);
		verify(voteRepository).getVoteCount(vote.getReview().getId(), vote.getVoteChoice());
	}

	@Nested
	@DisplayName("deleteVoteFail")
	class DeleteVoteFail {

		@Test
		@DisplayName("Fail - 삭제하려는 후기 투표가 존재하지 않으면 후기 투표 삭제에 실패한다")
		void deleteVoteFailByNotFoundVote() {
			// given
			Long wrongVoteId = 0L;
			when(voteRepository.findById(wrongVoteId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> voteService.deleteVote(wrongVoteId, member.getId()))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(voteRepository).findById(wrongVoteId);
		}

		@Test
		@DisplayName("Fail - 삭제하려는 후기 투표의 투표자가 아니면 후기 투표 삭제에 실패한다")
		void deleteVoteFailByUnAuthorized() {
			// given
			Long wrongMemberId = 0L;
			when(voteRepository.findById(vote.getId())).thenReturn(Optional.of(vote));

			// when & then
			assertThatThrownBy(() -> voteService.deleteVote(vote.getId(), wrongMemberId))
					.isExactlyInstanceOf(AuthenticationException.class)
					.hasMessage(UNAUTHORIZED.getMessage());
			verify(voteRepository).findById(vote.getId());
		}
	}

	@Test
	@DisplayName("Success - 로그인한 경우 후기 투표 정보 조회 시 투표 수와 투표 여부 반환에 성공한다")
	void getVotesSuccessWhenLogin() {
		// given
		AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), MemberAuthority.USER);
		VotesGetRequest votesGetRequest
				= new VotesGetRequest(publishedReview.getId(), Optional.of(authenticationDTO));
		when(reviewRepository.findById(votesGetRequest.reviewId())).thenReturn(Optional.of(publishedReview));
		when(memberRepository.findById(votesGetRequest.authenticationDTO().get().memberId()))
				.thenReturn(Optional.of(member));
		when(voteRepository.findVoteByMemberAndReview(any(Member.class), any(Review.class)))
				.thenReturn(Optional.of(vote));

		// when
		VotesResponse votesResponse = voteService.getVotes(votesGetRequest);

		// then
		verify(reviewRepository).findById(votesGetRequest.reviewId());
		verify(memberRepository).findById(votesGetRequest.authenticationDTO().get().memberId());
		verify(voteRepository).findVoteByMemberAndReview(any(Member.class), any(Review.class));
		assertThat(votesResponse.likeCount()).isEqualTo(publishedReview.getLikeCount());
		assertThat(votesResponse.dislikeCount()).isEqualTo(publishedReview.getLikeCount());
		assertThat(votesResponse.clickLike()).isEqualTo(vote.isLike());
		assertThat(votesResponse.clickDislike()).isEqualTo(vote.isDislike());
	}

	@Test
	@DisplayName("Success - 비로그인인 경우 후기 투표 정보 조회에 성공한다")
	void getVotesSuccessWhenNotLogin() {
		// given
		VotesGetRequest votesGetRequest
				= new VotesGetRequest(publishedReview.getId(), Optional.empty());
		when(reviewRepository.findById(votesGetRequest.reviewId())).thenReturn(Optional.of(publishedReview));

		// when
		VotesResponse votesResponse = voteService.getVotes(votesGetRequest);

		// then
		verify(reviewRepository).findById(votesGetRequest.reviewId());
		assertThat(votesResponse.likeCount()).isEqualTo(publishedReview.getLikeCount());
		assertThat(votesResponse.dislikeCount()).isEqualTo(publishedReview.getLikeCount());
		assertThat(votesResponse.clickLike()).isFalse();
		assertThat(votesResponse.clickDislike()).isFalse();
	}

	@Nested
	@DisplayName("getVotesFail")
	class GetVotesFail {
		@Test
		@DisplayName("Fail - 연관된 후기가 없으면 후기 투표 조회에 실패한다")
		void getVotesFailByNotFoundReview() {
			// given
			AuthenticationDTO authenticationDTO = new AuthenticationDTO(member.getId(), MemberAuthority.USER);
			VotesGetRequest votesGetRequest
					= new VotesGetRequest(publishedReview.getId(), Optional.of(authenticationDTO));
			when(reviewRepository.findById(votesGetRequest.reviewId())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> voteService.getVotes(votesGetRequest))
					.isExactlyInstanceOf(NotFoundException.class)
					.hasMessage(NOT_FOUND.getMessage());
			verify(reviewRepository).findById(votesGetRequest.reviewId());
		}
	}
}