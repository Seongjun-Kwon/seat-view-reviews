package com.goodseats.seatviewreviews.domain.review.scheduler;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.IntegerCodec;
import org.springframework.test.util.ReflectionTestUtils;

import com.goodseats.seatviewreviews.common.error.exception.ErrorCode;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;

@ExtendWith(MockitoExtension.class)
class ReviewSchedulerTest {

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private RScoredSortedSet<Object> reviewAndViewCountLogs;

	@Mock
	private RSet<Object> userViewedReviewLogs;

	@Mock
	private RKeys rKeys;

	@InjectMocks
	private ReviewScheduler reviewScheduler;

	private Member member;
	private Stadium stadium;
	private SeatGrade seatGrade;
	private SeatSection seatSection;
	private Seat seat;
	private Review review;

	@BeforeEach
	void setUp() {
		member = new Member("test@test.com", "test", "test");
		ReflectionTestUtils.setField(member, "id", 1L);

		stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		seatSection = new SeatSection("110", stadium, seatGrade);
		seat = new Seat("1", seatGrade, seatSection);
		ReflectionTestUtils.setField(seat, "id", 1L);

		review = new Review(member, seat);
		review.publish("테스트 제목", "테스트 내용", 5);
		ReflectionTestUtils.setField(review, "id", 1L);
	}

	@Test
	@DisplayName("Success - 스케줄링 주기마다 redis 에 저장된 조회 수를 DB 에 동기화한다")
	void syncViewCountToDBSuccess() {
		// given
		List<String> keys = new ArrayList<>();
		String key = REVIEW_AND_VIEW_COUNT_LOGS_NAME + review.getId();
		keys.add(key);

		double nowTime = System.currentTimeMillis();
		int latestViewCount = 100;

		when(redissonClient.getKeys()).thenReturn(rKeys);
		when(rKeys.getKeysStreamByPattern(REVIEW_AND_VIEW_COUNT_LOGS_NAME_PATTERN)).thenReturn(keys.stream());
		when(redissonClient.getScoredSortedSet(eq(key), any(IntegerCodec.class))).thenReturn(reviewAndViewCountLogs);
		when(reviewAndViewCountLogs.last()).thenReturn(latestViewCount);
		when(reviewAndViewCountLogs.getScore(latestViewCount)).thenReturn(nowTime);
		when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

		// when
		reviewScheduler.syncViewCountToDB();

		// then
		verify(redissonClient).getKeys();
		verify(rKeys).getKeysStreamByPattern(REVIEW_AND_VIEW_COUNT_LOGS_NAME_PATTERN);
		verify(redissonClient, times(2)).getScoredSortedSet(eq(key), any(IntegerCodec.class));
		verify(reviewRepository).findById(review.getId());
		assertThat(review.getViewCount()).isEqualTo(latestViewCount);
	}

	@Test
	@DisplayName("Fail - redis 에 저장된 후기가 DB 에 없으면 동기화에 실패한다")
	void syncViewCountToDBFailByNotFoundReview() {
		// given
		Long wrongReviewId = 2L;

		List<String> keys = new ArrayList<>();
		String key = REVIEW_AND_VIEW_COUNT_LOGS_NAME + wrongReviewId;
		keys.add(key);

		double nowTime = System.currentTimeMillis();
		int latestViewCount = 100;

		when(redissonClient.getKeys()).thenReturn(rKeys);
		when(rKeys.getKeysStreamByPattern(REVIEW_AND_VIEW_COUNT_LOGS_NAME_PATTERN)).thenReturn(keys.stream());
		when(redissonClient.getScoredSortedSet(eq(key), any(IntegerCodec.class))).thenReturn(reviewAndViewCountLogs);
		when(reviewAndViewCountLogs.last()).thenReturn(latestViewCount);
		when(reviewAndViewCountLogs.getScore(latestViewCount)).thenReturn(nowTime);
		when(reviewRepository.findById(wrongReviewId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewScheduler.syncViewCountToDB())
				.isExactlyInstanceOf(NotFoundException.class)
				.hasMessage(ErrorCode.NOT_FOUND.getMessage());
		verify(redissonClient).getKeys();
		verify(rKeys).getKeysStreamByPattern(REVIEW_AND_VIEW_COUNT_LOGS_NAME_PATTERN);
		verify(redissonClient).getScoredSortedSet(eq(key), any(IntegerCodec.class));
		verify(reviewRepository).findById(wrongReviewId);
	}

	@Test
	@DisplayName("Success - 스케줄링 주기마다 redis 에 저장된 조회 수 데이터를 제거한다")
	void clearViewCountLogsSuccess() {
		// given
		List<String> keys = new ArrayList<>();
		String key = REVIEW_AND_VIEW_COUNT_LOGS_NAME + review.getId();
		keys.add(key);

		when(redissonClient.getSet(USER_VIEWED_REVIEW_LOGS_NAME)).thenReturn(userViewedReviewLogs);
		when(userViewedReviewLogs.delete()).thenReturn(true);
		when(redissonClient.getKeys()).thenReturn(rKeys);
		when(rKeys.getKeysStreamByPattern(REVIEW_AND_VIEW_COUNT_LOGS_NAME_PATTERN)).thenReturn(keys.stream());
		when(redissonClient.getScoredSortedSet(any(String.class))).thenReturn(reviewAndViewCountLogs);
		when(reviewAndViewCountLogs.delete()).thenReturn(true);

		// when
		reviewScheduler.clearViewCountLogs();

		// then
		verify(redissonClient).getSet(USER_VIEWED_REVIEW_LOGS_NAME);
		verify(userViewedReviewLogs).delete();
		verify(redissonClient).getKeys();
		verify(rKeys).getKeysStreamByPattern(REVIEW_AND_VIEW_COUNT_LOGS_NAME_PATTERN);
		verify(redissonClient).getScoredSortedSet(any(String.class));
		verify(reviewAndViewCountLogs).delete();
	}
}