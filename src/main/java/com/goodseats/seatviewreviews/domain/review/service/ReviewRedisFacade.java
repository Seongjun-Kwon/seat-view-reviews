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
		ReviewDetailResponse reviewDetailResponse = reviewService.getReview(reviewId);
		controlViewCountConcurrency(userKey, reviewId, reviewDetailResponse);
		return reviewDetailResponse;
	}

	public void clearAllLogs() {
		RSet<String> userViewedReviewLogs = redissonClient.getSet(USER_VIEWED_REVIEW_LOGS_NAME);
		RMap<String, Integer> reviewAndViewCountLogs = redissonClient.getMap(REVIEW_AND_VIEW_COUNT_LOGS_NAME);

		if (userViewedReviewLogs.isExists()) {
			userViewedReviewLogs.clear();
		}
		if (reviewAndViewCountLogs.isExists()) {
			reviewAndViewCountLogs.clear();
		}
	}

	private void controlViewCountConcurrency(String userKey, Long reviewId, ReviewDetailResponse reviewDetailResponse) {
		RLock viewCountLock = redissonClient.getLock(LOCK_NAME);

		try {
			tryLock(viewCountLock);

			String userViewedReviewLog = generateUserViewedReviewLog(userKey, reviewId);

			if (alreadyViewedReview(userViewedReviewLog)) {
				return;
			}

			saveUserViewedReviewLog(userViewedReviewLog);
			increaseViewCount(reviewId, reviewDetailResponse.viewCount());

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			viewCountLock.unlock();
		}
	}

	private void tryLock(RLock viewCountLock) throws InterruptedException {
		boolean available = viewCountLock.tryLock(LOCK_WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);

		if (!available) {
			throw new InterruptedException();
		}
	}

	private String generateUserViewedReviewLog(String userKey, Long reviewId) {
		return "user" + "_" + userKey + ", " + "reviewId" + "_" + reviewId;
	}

	private boolean alreadyViewedReview(String userViewedReviewLog) {
		return redissonClient.getSet(USER_VIEWED_REVIEW_LOGS_NAME).contains(userViewedReviewLog);
	}

	private void saveUserViewedReviewLog(String userViewedReviewLog) {
		redissonClient.getSet(USER_VIEWED_REVIEW_LOGS_NAME).add(userViewedReviewLog);
	}

	private void increaseViewCount(Long reviewId, int defaultViewCount) {
		RMap<String, String> reviewAndViewCountLogs = redissonClient.getMap(REVIEW_AND_VIEW_COUNT_LOGS_NAME);
		int latestViewCount = getLatestViewCount(reviewAndViewCountLogs, reviewId, defaultViewCount);
		String reviewAndViewCountLogsKey = generateReviewAndViewCountLogsKey(reviewId);
		reviewAndViewCountLogs.put(reviewAndViewCountLogsKey, String.valueOf(latestViewCount + 1));
	}

	private String generateReviewAndViewCountLogsKey(Long reviewId) {
		String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		return "reviewId" + "_" + reviewId + ", " + "viewedTime" + "_" + nowTime;
	}

	private int getLatestViewCount(
			RMap<String, String> reviewAndViewCountLogs, Long reviewId, int defaultViewCount
	) {
		return reviewAndViewCountLogs.keySet()
				.stream()
				.map(String::valueOf)
				.collect(Collectors.toSet())
				.stream()
				.filter(key -> extractReviewId(key).equals(String.valueOf(reviewId)))
				.max(Comparator.comparing(this::extractViewedTime))
				.map(latestKey -> Integer.parseInt(reviewAndViewCountLogs.get(latestKey)))
				.orElse(defaultViewCount);
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
}
