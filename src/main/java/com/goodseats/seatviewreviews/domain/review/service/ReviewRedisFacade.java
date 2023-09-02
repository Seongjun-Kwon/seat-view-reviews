package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.IntegerCodec;
import org.springframework.stereotype.Component;

import com.goodseats.seatviewreviews.domain.member.model.dto.AuthenticationDTO;
import com.goodseats.seatviewreviews.domain.review.model.dto.response.ReviewDetailResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewRedisFacade {

	private final RedissonClient redissonClient;
	private final ReviewService reviewService;

	public ReviewDetailResponse getReview(AuthenticationDTO authenticationDTO, Long reviewId) {
		if (Objects.isNull(authenticationDTO)) {
			return reviewService.getReview(reviewId);
		}
		if (doNotViewReview(authenticationDTO.memberId(), reviewId)) {
			controlViewCountConcurrency(authenticationDTO.memberId(), reviewId);
		}

		return reviewService.getReview(reviewId);
	}

	public int getLatestViewCount(Long reviewId, int defaultViewCount) {
		RScoredSortedSet<Integer> reviewAndViewCountLogs
				= redissonClient.getScoredSortedSet(generateReviewAndViewCountLogsKey(reviewId), new IntegerCodec());

		if (reviewAndViewCountLogs.isExists()) {
			return reviewAndViewCountLogs.last();
		}
		return defaultViewCount;
	}

	private void controlViewCountConcurrency(Long memberId, Long reviewId) {
		RLock viewCountLock = redissonClient.getLock(VIEW_COUNT_LOCK);

		try {
			tryLock(viewCountLock);

			String userViewedReviewLog = generateUserViewedReviewLog(memberId, reviewId);
			ReviewDetailResponse reviewDetailResponse = reviewService.getReview(reviewId);
			saveUserViewedReviewLog(userViewedReviewLog);
			increaseViewCount(reviewId, reviewDetailResponse.viewCount());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			if (viewCountLock.isHeldByCurrentThread()) {
				viewCountLock.unlock();
			}
		}
	}

	private void tryLock(RLock viewCountLock) throws InterruptedException {
		boolean available = viewCountLock.tryLock(LOCK_WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);

		if (!available) {
			throw new InterruptedException();
		}
	}

	private String generateUserViewedReviewLog(Long memberId, Long reviewId) {
		return "memberId" + "_" + memberId + ", " + "reviewId" + "_" + reviewId;
	}

	private boolean doNotViewReview(Long memberId, Long reviewId) {
		String userViewedReviewLog = generateUserViewedReviewLog(memberId, reviewId);
		return !redissonClient.getSet(USER_VIEWED_REVIEW_LOGS_NAME).contains(userViewedReviewLog);
	}

	private void saveUserViewedReviewLog(String userViewedReviewLog) {
		redissonClient.getSet(USER_VIEWED_REVIEW_LOGS_NAME).add(userViewedReviewLog);
	}

	private void increaseViewCount(Long reviewId, int defaultViewCount) {
		RScoredSortedSet<Integer> reviewAndViewCountLogs
				= redissonClient.getScoredSortedSet(generateReviewAndViewCountLogsKey(reviewId), new IntegerCodec());

		int latestViewCount = getLatestViewCount(reviewId, defaultViewCount);
		long nowUnixTimeMillis = System.currentTimeMillis();
		reviewAndViewCountLogs.add((double)nowUnixTimeMillis, latestViewCount + 1);
	}

	private String generateReviewAndViewCountLogsKey(Long reviewId) {
		return REVIEW_AND_VIEW_COUNT_LOGS_NAME + reviewId;
	}
}