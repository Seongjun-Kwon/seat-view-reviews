package com.goodseats.seatviewreviews.domain.vote.service;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.goodseats.seatviewreviews.domain.vote.model.dto.request.ReviewVoteCreateRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewVoteRedisFacade {

	private final RedissonClient redissonClient;
	private final ReviewVoteService reviewVoteService;

	public Long createVote(ReviewVoteCreateRequest reviewVoteCreateRequest, Long memberId) {
		RLock reviewVoteLock = redissonClient.getLock(REVIEW_VOTE_LOCK);
		Long reviewVoteId;

		try {
			tryLock(reviewVoteLock);
			reviewVoteId = reviewVoteService.createVote(reviewVoteCreateRequest, memberId);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			if (reviewVoteLock.isHeldByCurrentThread()) {
				reviewVoteLock.unlock();
			}
		}

		return reviewVoteId;
	}

	public void deleteVote(Long reviewVoteId, Long memberId) {
		RLock reviewVoteLock = redissonClient.getLock(REVIEW_VOTE_LOCK);

		try {
			tryLock(reviewVoteLock);
			reviewVoteService.deleteVote(reviewVoteId, memberId);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			if (reviewVoteLock.isHeldByCurrentThread()) {
				reviewVoteLock.unlock();
			}
		}
	}

	private void tryLock(RLock reviewVoteLock) throws InterruptedException {
		boolean available = reviewVoteLock.tryLock(LOCK_WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);

		if (!available) {
			throw new InterruptedException();
		}
	}
}