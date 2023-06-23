package com.goodseats.seatviewreviews.domain.review.scheduler;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;
import static com.goodseats.seatviewreviews.common.constant.SchedulerConstant.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.domain.review.repository.ReviewRepository;
import com.goodseats.seatviewreviews.domain.review.service.ReviewRedisFacade;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewScheduler {

	private final ReviewRedisFacade reviewRedisFacade;
	private final RedissonClient redissonClient;
	private final ReviewRepository reviewRepository;

	@Transactional
	@Scheduled(cron = "0 */5 * * * *")
	public void syncViewCountToDB() {
		RMap<String, String> reviewAndViewCountLogs = redissonClient.getMap(REVIEW_AND_VIEW_COUNT_LOGS_NAME);
		String previousScheduledMinute = getPreviousScheduledMinute();
		List<String> targetKeys = getTargetsToSync(previousScheduledMinute, reviewAndViewCountLogs);
		updateViewCount(reviewAndViewCountLogs, targetKeys);
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void clearHitLogs() {
		reviewRedisFacade.clearAllLogs();
	}

	private String getPreviousScheduledMinute() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime previousScheduledMinute = now.minusMinutes(SCHEDULING_MINUTE);
		return previousScheduledMinute.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	private List<String> getTargetsToSync(String previousScheduledMinute, RMap<String, String> reviewAndViewCountLogs) {
		return reviewAndViewCountLogs.keySet().stream()
				.filter(key -> isTargetToSync(previousScheduledMinute, key))
				.collect(Collectors.toMap(
						this::extractReviewId,
						Function.identity(),
						BinaryOperator.maxBy(Comparator.comparing(this::extractViewedTime))
				))
				.values()
				.stream()
				.toList();
	}

	private boolean isTargetToSync(String previousScheduledMinute, String key) {
		int beforeViewedTimeIndex = key.lastIndexOf(DELIMITER);
		String viewedTime = key.substring(beforeViewedTimeIndex + 1);
		return viewedTime.compareTo(previousScheduledMinute) >= 0;
	}

	private String extractReviewId(String key) {
		int beforeReviewIdIndex = key.indexOf(DELIMITER);
		int afterReviewIdIndex = key.indexOf(SEPARATOR);
		return key.substring(beforeReviewIdIndex + 1, afterReviewIdIndex);
	}

	private LocalDateTime extractViewedTime(String key) {
		int beforeTimeIndex = key.lastIndexOf(DELIMITER);
		String timeString = key.substring(beforeTimeIndex + 1);
		return LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	private void updateViewCount(RMap<String, String> reviewAndViewCountLogs, List<String> targetKeys) {
		targetKeys
				.forEach(key -> {
					int viewCount = Integer.parseInt(reviewAndViewCountLogs.get(key));
					Long reviewId = Long.valueOf(extractReviewId(key));
					reviewRepository.updateViewCount(viewCount, reviewId);
				});
	}
}
