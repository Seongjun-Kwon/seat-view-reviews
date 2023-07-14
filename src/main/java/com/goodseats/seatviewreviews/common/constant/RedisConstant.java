package com.goodseats.seatviewreviews.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisConstant {

	public static final String REDISSON_ADDRESS_PREFIX = "redis://";
	public static final String USER_VIEWED_REVIEW_LOGS_NAME = "userViewedReviewLogs";
	public static final String REVIEW_AND_VIEW_COUNT_LOGS_NAME = "reviewViewCountLogs";
	public static final String DELIMITER = "_";
	public static final String SEPARATOR = ", ";
	public static final String LOCK_NAME = "viewCountLock";
	public static final int LOCK_WAIT_TIME = 10;
	public static final int LEASE_TIME = 5;

}
