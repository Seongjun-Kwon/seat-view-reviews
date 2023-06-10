package com.goodseats.seatviewreviews.domain.review.scheduler;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;
import static com.goodseats.seatviewreviews.common.constant.SchedulerConstant.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
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
	public void syncHitsToDB() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startTime = now.minusMinutes(SCHEDULED_MINUTE);
		String startTimeString = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		RMap<String, String> reviewHitsLogs = redissonClient.getMap(REVIEW_HITS_LOGS_NAME);

		List<String> targetKeys = reviewHitsLogs.keySet().stream()
				.filter(key -> isTarget(startTimeString, key))
				.collect(Collectors.groupingBy(
								this::extractReviewId,
								Collectors.collectingAndThen(
										Collectors.maxBy(Comparator.comparing(this::extractTime)),
										optionalKey -> optionalKey.orElse(null)
								)
						)
				)
				.values()
				.stream()
				.toList();

		targetKeys
				.forEach(key -> {
					int hits = Integer.parseInt(reviewHitsLogs.get(key));
					Long reviewId = Long.valueOf(extractReviewId(key));
					reviewRepository.updateHits(hits, reviewId);
				});
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void clearHitLogs() {
		reviewRedisFacade.clearHitLogs();
	}

	private boolean isTarget(String startTimeString, String key) {
		int beforeTimeIndex = key.lastIndexOf(DELIMITER);
		String timeString = key.substring(beforeTimeIndex + 1);
		return timeString.compareTo(startTimeString) >= 0;
	}

	private String extractReviewId(String key) {
		int beforeReviewIdIndex = key.indexOf(DELIMITER);
		int afterReviewIdIndex = key.indexOf(SEPARATOR);
		return key.substring(beforeReviewIdIndex + 1, afterReviewIdIndex);
	}

	private LocalDateTime extractTime(String key) {
		int beforeTimeIndex = key.lastIndexOf(DELIMITER);
		String timeString = key.substring(beforeTimeIndex + 1);
		return LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}
}
