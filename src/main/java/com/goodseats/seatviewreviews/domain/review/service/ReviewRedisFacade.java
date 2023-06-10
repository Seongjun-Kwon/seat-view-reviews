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

		RLock hitsLock = redissonClient.getLock("hitsLock");
		ReviewDetailResponse reviewDetailResponse = reviewService.getReview(reviewId);

		try {
			boolean available = hitsLock.tryLock(2, 2, TimeUnit.SECONDS);
			if (!available) {
				throw new InterruptedException();
			}

			String userReviewViewLog = generateUserReviewViewLog(userKey, reviewId);
			if (alreadyViewedReview(userReviewViewLog)) {
				return reviewDetailResponse;
			}

			saveUserReviewViewLog(userReviewViewLog);
			increaseReviewHits(reviewId, reviewDetailResponse.hits());

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			hitsLock.unlock();
		}

		return reviewDetailResponse;
	}

	public void clearHitLogs() {
		RSet<String> userHitsLogSet = redissonClient.getSet(USER_REVIEW_VIEW_LOGS_NAME);
		RMap<String, Integer> reviewHitsTimeLogMap = redissonClient.getMap(REVIEW_HITS_LOGS_NAME);

		if (userHitsLogSet.isExists()) {
			userHitsLogSet.clear();
		}
		if (reviewHitsTimeLogMap.isExists()) {
			reviewHitsTimeLogMap.clear();
		}
	}

	private boolean alreadyViewedReview(String userReviewViewLog) {
		return redissonClient.getSet(USER_REVIEW_VIEW_LOGS_NAME).contains(userReviewViewLog);
	}

	private void saveUserReviewViewLog(String userReviewViewLog) {
		redissonClient.getSet(USER_REVIEW_VIEW_LOGS_NAME).add(userReviewViewLog);
	}

	private void increaseReviewHits(Long reviewId, int defaultHits) {
		RMap<String, String> reviewHitsLogs = redissonClient.getMap(REVIEW_HITS_LOGS_NAME);

		String reviewHitsLogsKey = generateReviewHitsLogKey(reviewId);
		String latestHitsKey = getLatestHitsKey(reviewHitsLogs, reviewId, reviewHitsLogsKey);
		int latestHits = Integer.parseInt(reviewHitsLogs.getOrDefault(latestHitsKey, String.valueOf(defaultHits)));

		reviewHitsLogs.put(reviewHitsLogsKey, String.valueOf(latestHits + 1));
	}

	private String generateUserReviewViewLog(String userKey, Long reviewId) {
		return "user" + DELIMITER + userKey + SEPARATOR + "reviewId" + DELIMITER + reviewId;
	}

	private String generateReviewHitsLogKey(Long reviewId) {
		String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		return "reviewId" + DELIMITER + reviewId + SEPARATOR + "time" + DELIMITER + nowTime;
	}

	private String getLatestHitsKey(RMap<String, String> reviewHitsLogs, Long reviewId, String reviewHitsLogsKey) {
		return reviewHitsLogs.keySet()
				.stream()
				.map(String::valueOf)
				.collect(Collectors.toSet())
				.stream()
				.filter(key -> extractReviewId(key).equals(String.valueOf(reviewId)))
				.max(Comparator.comparing(this::extractTime))
				.orElse(reviewHitsLogsKey);
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
