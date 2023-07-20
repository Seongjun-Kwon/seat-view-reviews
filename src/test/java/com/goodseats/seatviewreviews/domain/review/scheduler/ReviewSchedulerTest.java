package com.goodseats.seatviewreviews.domain.review.scheduler;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import com.goodseats.seatviewreviews.common.error.exception.ErrorCode;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.member.model.entity.Member;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.review.service.ReviewRedisFacade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;

@ExtendWith(MockitoExtension.class)
class ReviewSchedulerTest {

	@Mock
	private ReviewRedisFacade reviewRedisFacade;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private RMap<Object, Object> reviewAndViewCountLogs;

	@InjectMocks
	private ReviewScheduler reviewScheduler;

	@Test
	@DisplayName("Success - 스케줄링 주기마다 redis 에 저장된 조회 수를 DB 에 동기화한다")
	void syncViewCountToDBSuccess() {
		// given
		Long seatId = 1L;
		Long memberId = 1L;
		Long reviewId = 1L;

		Member member = new Member("test@test.com", "test", "test");
		ReflectionTestUtils.setField(member, "id", memberId);

		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		SeatSection seatSection = new SeatSection("110", stadium, seatGrade);
		Seat seat = new Seat("1", seatGrade, seatSection);
		ReflectionTestUtils.setField(seat, "id", seatId);

		Review review = new Review(member, seat);
		review.publish("테스트 제목", "테스트 내용", 5);
		ReflectionTestUtils.setField(review, "id", reviewId);

		String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
		String key = "reviewId" + "_" + reviewId + ", " + "viewedTime" + "_" + nowTime;
		Set<Object> keySet = new HashSet<>();
		keySet.add(key);

		String latestViewCount = "100";

		when(redissonClient.getMap(REVIEW_AND_VIEW_COUNT_LOGS_NAME)).thenReturn(reviewAndViewCountLogs);
		when(reviewAndViewCountLogs.keySet()).thenReturn(keySet);
		when(reviewAndViewCountLogs.get(key)).thenReturn(latestViewCount);
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		// when
		reviewScheduler.syncViewCountToDB();

		// then
		verify(redissonClient, times(2)).getMap(REVIEW_AND_VIEW_COUNT_LOGS_NAME);
		verify(reviewAndViewCountLogs).keySet();
		verify(reviewAndViewCountLogs).get(key);
		verify(reviewRepository).findById(reviewId);
		assertThat(review.getViewCount()).isEqualTo(Integer.parseInt(latestViewCount));
	}

	@Test
	@DisplayName("Fail - redis 에 저장된 후기가 DB 에 없으면 동기화에 실패한다")
	void syncViewCountToDBFailByNotFoundReview() {
		// given
		Long reviewId = 1L;

		String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
		String key = "reviewId" + "_" + reviewId + ", " + "viewedTime" + "_" + nowTime;
		Set<Object> keySet = new HashSet<>();
		keySet.add(key);

		String latestViewCount = "100";

		when(redissonClient.getMap(REVIEW_AND_VIEW_COUNT_LOGS_NAME)).thenReturn(reviewAndViewCountLogs);
		when(reviewAndViewCountLogs.keySet()).thenReturn(keySet);
		when(reviewAndViewCountLogs.get(key)).thenReturn(latestViewCount);
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewScheduler.syncViewCountToDB())
				.isExactlyInstanceOf(NotFoundException.class)
				.hasMessage(ErrorCode.NOT_FOUND.getMessage());
		verify(redissonClient, times(2)).getMap(REVIEW_AND_VIEW_COUNT_LOGS_NAME);
		verify(reviewAndViewCountLogs).keySet();
		verify(reviewAndViewCountLogs).get(key);
		verify(reviewRepository).findById(reviewId);
	}

	@Test
	@DisplayName("Success - 스케줄링 주기마다 redis 에 저장된 조회 수 데이터를 제거한다")
	void clearViewCountLogsSuccess() {
		// given
		doNothing().when(reviewRedisFacade).clearAllLogs();

		// when
		reviewScheduler.clearViewCountLogs();

		// then
		verify(reviewRedisFacade).clearAllLogs();
	}
}