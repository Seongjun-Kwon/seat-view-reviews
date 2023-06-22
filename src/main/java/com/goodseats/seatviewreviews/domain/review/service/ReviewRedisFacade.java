package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewDetailResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewRedisFacade {

	private final RedissonClient redissonClient;
	private final ReviewService reviewService;

	public ReviewDetailResponse getReview(String userKey, Long reviewId) {

		RLock viewCountLock = redissonClient.getLock(LOCK_NAME);
		ReviewDetailResponse reviewDetailResponse = reviewService.getReview(reviewId);

		try {
			boolean available = viewCountLock.tryLock(LOCK_WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
			if (!available) {
				throw new InterruptedException();
			}

			String userReviewViewLog = generateUserReviewViewLog(userKey, reviewId);
			if (alreadyViewedReview(userReviewViewLog)) {
				return reviewDetailResponse;
			}

			saveUserReviewViewLog(userReviewViewLog);
			increaseReviewViewCount(reviewId, reviewDetailResponse.viewCount());

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			viewCountLock.unlock();
		}

		return reviewDetailResponse;
	}

	public void clearHitLogs() {
		RSet<String> userViewCountLogSet = redissonClient.getSet(USER_REVIEW_VIEW_LOGS_NAME);
		RMap<String, Integer> reviewViewCountTimeLogMap = redissonClient.getMap(REVIEW_VIEW_COUNT_LOGS_NAME);

		if (userViewCountLogSet.isExists()) {
			userViewCountLogSet.clear();
		}
		if (reviewViewCountTimeLogMap.isExists()) {
			reviewViewCountTimeLogMap.clear();
		}
	}

	private boolean alreadyViewedReview(String userReviewViewLog) {
		return redissonClient.getSet(USER_REVIEW_VIEW_LOGS_NAME).contains(userReviewViewLog);
	}

	private void saveUserReviewViewLog(String userReviewViewLog) {
		redissonClient.getSet(USER_REVIEW_VIEW_LOGS_NAME).add(userReviewViewLog);
	}

	private void increaseReviewViewCount(Long reviewId, int defaultViewCount) {
		RMap<String, String> reviewViewCountLogs = redissonClient.getMap(REVIEW_VIEW_COUNT_LOGS_NAME);

		String reviewViewCountLogsKey = generateReviewViewCountLogKey(reviewId);
		String latestViewCountKey = getLatestViewCountKey(reviewViewCountLogs, reviewId, reviewViewCountLogsKey);
		int latestViewCount = Integer.parseInt(
				reviewViewCountLogs.getOrDefault(latestViewCountKey, String.valueOf(defaultViewCount)));

		reviewViewCountLogs.put(reviewViewCountLogsKey, String.valueOf(latestViewCount + 1));
	}

	private String generateUserReviewViewLog(String userKey, Long reviewId) {
		return "user" + DELIMITER + userKey + SEPARATOR + "reviewId" + DELIMITER + reviewId;
	}

	private String generateReviewViewCountLogKey(Long reviewId) {
		String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		return "reviewId" + DELIMITER + reviewId + SEPARATOR + "time" + DELIMITER + nowTime;
	}

	private String getLatestViewCountKey(
			RMap<String, String> reviewViewCountLogs, Long reviewId, String reviewViewCountLogsKey
	) {
		return reviewViewCountLogs.keySet()
				.stream()
				.map(String::valueOf)
				.collect(Collectors.toSet())
				.stream()
				.filter(key -> extractReviewId(key).equals(String.valueOf(reviewId)))
				.max(Comparator.comparing(this::extractTime))
				.orElse(reviewViewCountLogsKey);
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
