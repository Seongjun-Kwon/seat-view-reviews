package com.goodseats.seatviewreviews.domain.review.scheduler;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;
import static com.goodseats.seatviewreviews.common.constant.SchedulerConstant.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.IntegerCodec;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.common.error.exception.ErrorCode;
import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.review.model.entity.Review;
import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewScheduler {

	private final RedissonClient redissonClient;
	private final ReviewRepository reviewRepository;

	@Transactional
	@Scheduled(cron = "0 */5 * * * *")
	public void syncViewCountToDB() {
		LocalDateTime previousScheduledTime = getPreviousScheduledMinute();
		List<String> reviewViewCountLogsViewedAfterPreviousScheduledTime
				= getReviewViewCountLogsViewedAfterPreviousScheduledTime(previousScheduledTime);
		updateReviewViewCount(reviewViewCountLogsViewedAfterPreviousScheduledTime);
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void clearViewCountLogs() {
		redissonClient.getSet(USER_VIEWED_REVIEW_LOGS_NAME).delete();
		redissonClient.getKeys()
				.getKeysStreamByPattern(REVIEW_AND_VIEW_COUNT_LOGS_NAME_PATTERN)
				.forEach(reviewAndViewCountLogsKey -> redissonClient.getScoredSortedSet(reviewAndViewCountLogsKey).delete());
	}

	private LocalDateTime getPreviousScheduledMinute() {
		LocalDateTime now = LocalDateTime.now();
		return now.minusMinutes(VIEW_COUNT_SCHEDULING_MINUTE);
	}

	private List<String> getReviewViewCountLogsViewedAfterPreviousScheduledTime(LocalDateTime previousScheduledTime) {
		return redissonClient.getKeys()
				.getKeysStreamByPattern(REVIEW_AND_VIEW_COUNT_LOGS_NAME_PATTERN)
				.filter(reviewAndViewCountLogsName
						-> lastViewedAfterPreviousScheduledTime(reviewAndViewCountLogsName, previousScheduledTime)
				)
				.toList();
	}

	private boolean lastViewedAfterPreviousScheduledTime(String logName, LocalDateTime previousScheduledTime) {
		RScoredSortedSet<Integer> reviewAndViewCountLogs = getReviewAndViewCountLogs(logName);

		LocalDateTime lastViewedTime
				= convertEpochMilliToLocalDateTime(reviewAndViewCountLogs.getScore(reviewAndViewCountLogs.last()).longValue());
		return lastViewedTime.isAfter(previousScheduledTime);
	}

	private RScoredSortedSet<Integer> getReviewAndViewCountLogs(String name) {
		return redissonClient.getScoredSortedSet(name, new IntegerCodec());
	}

	private LocalDateTime convertEpochMilliToLocalDateTime(long epochMillis) {
		Instant instant = Instant.ofEpochMilli(epochMillis);
		ZoneId zoneId = ZoneId.of("Asia/Seoul");
		return instant.atZone(zoneId).toLocalDateTime();
	}

	private void updateReviewViewCount(List<String> reviewViewCountLogs) {
		reviewViewCountLogs
				.forEach(logName -> {
					Review review = reviewRepository.findById(extractReviewId(logName))
							.orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
					review.updateViewCount(getReviewAndViewCountLogs(logName).last());
				});
	}

	private Long extractReviewId(String key) {
		int beforeReviewIdIndex = REVIEW_AND_VIEW_COUNT_LOGS_NAME.length() - 1;
		return Long.parseLong(key.substring(beforeReviewIdIndex + 1));
	}
}