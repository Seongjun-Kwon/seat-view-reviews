package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewDetailResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewRedisFacade {

	private final RedissonClient redissonClient;
	private final ReviewService reviewService;

	@Transactional(readOnly = true)
	public ReviewDetailResponse getReview(String userKey, Long reviewId) {
		if (hasNotViewedReview(userKey, reviewId)) {
			controlViewCountConcurrency(userKey, reviewId);
		}

		return reviewService.getReview(reviewId);
	}

	public int getLatestViewCount(Long reviewId, int defaultViewCount) {
		RMap<String, String> reviewAndViewCountLogs = redissonClient.getMap(REVIEW_AND_VIEW_COUNT_LOGS_NAME);

		return reviewAndViewCountLogs.keySet()
				.stream()
				.filter(key -> extractReviewId(key).equals(String.valueOf(reviewId)))
				.max(Comparator.comparing(this::extractViewedTime))
				.map(latestKey -> Integer.parseInt(reviewAndViewCountLogs.get(latestKey)))
				.orElse(defaultViewCount);
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

	private void controlViewCountConcurrency(String userKey, Long reviewId) {
		RLock viewCountLock = redissonClient.getLock(LOCK_NAME);

		try {
			tryLock(viewCountLock);

			String userViewedReviewLog = generateUserViewedReviewLog(userKey, reviewId);
			ReviewDetailResponse reviewDetailResponse = reviewService.getReview(reviewId);
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

	private boolean hasNotViewedReview(String userKey, Long reviewId) {
		String userViewedReviewLog = generateUserViewedReviewLog(userKey, reviewId);
		return !redissonClient.getSet(USER_VIEWED_REVIEW_LOGS_NAME).contains(userViewedReviewLog);
	}

	private void saveUserViewedReviewLog(String userViewedReviewLog) {
		redissonClient.getSet(USER_VIEWED_REVIEW_LOGS_NAME).add(userViewedReviewLog);
	}

	private void increaseViewCount(Long reviewId, int defaultViewCount) {
		RMap<String, String> reviewAndViewCountLogs = redissonClient.getMap(REVIEW_AND_VIEW_COUNT_LOGS_NAME);
		int latestViewCount = getLatestViewCount(reviewId, defaultViewCount);
		String reviewAndViewCountLogsKey = generateReviewAndViewCountLogsKey(reviewId);
		reviewAndViewCountLogs.put(reviewAndViewCountLogsKey, String.valueOf(latestViewCount + 1));
	}

	private String generateReviewAndViewCountLogsKey(Long reviewId) {
		String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(VIEWED_TIME_FORMAT));
		return "reviewId" + "_" + reviewId + ", " + "viewedTime" + "_" + nowTime;
	}

	private String extractReviewId(String key) {
		int beforeReviewIdIndex = key.indexOf(DELIMITER);
		int afterReviewIdIndex = key.indexOf(SEPARATOR);
		return key.substring(beforeReviewIdIndex + 1, afterReviewIdIndex);
	}

	private LocalDateTime extractViewedTime(String key) {
		int beforeTimeIndex = key.lastIndexOf(DELIMITER);
		String timeString = key.substring(beforeTimeIndex + 1);
		return LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern(VIEWED_TIME_FORMAT));
	}
}