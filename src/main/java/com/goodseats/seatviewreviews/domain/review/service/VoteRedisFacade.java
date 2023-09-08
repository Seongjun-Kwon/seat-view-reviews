package com.goodseats.seatviewreviews.domain.review.service;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.goodseats.seatviewreviews.domain.review.model.dto.request.VoteCreateRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VoteRedisFacade {

	private final RedissonClient redissonClient;
	private final VoteService voteService;

	public Long createVote(VoteCreateRequest voteCreateRequest, Long memberId) {
		RLock voteLock = redissonClient.getLock(VOTE_LOCK + voteCreateRequest.reviewId());

		try {
			tryLock(voteLock);
			return voteService.createVote(voteCreateRequest, memberId);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			if (voteLock.isHeldByCurrentThread()) {
				voteLock.unlock();
			}
		}
	}

	public void deleteVote(Long voteId, Long memberId) {
		RLock voteLock = redissonClient.getLock(VOTE_LOCK + voteId);

		try {
			tryLock(voteLock);
			voteService.deleteVote(voteId, memberId);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			if (voteLock.isHeldByCurrentThread()) {
				voteLock.unlock();
			}
		}
	}

	private void tryLock(RLock voteLock) throws InterruptedException {
		boolean available = voteLock.tryLock(LOCK_WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);

		if (!available) {
			throw new InterruptedException();
		}
	}
}