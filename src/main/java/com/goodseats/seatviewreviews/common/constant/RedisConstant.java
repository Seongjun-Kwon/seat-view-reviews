package com.goodseats.seatviewreviews.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisConstant {

	public static final String REDISSON_ADDRESS_PREFIX = "redis://";
	public static final String USER_REVIEW_VIEW_LOGS_NAME = "userReviewViewLogs";
	public static final String REVIEW_HITS_LOGS_NAME = "reviewHitsLogs";
	public static final String DELIMITER = "_";
	public static final String SEPARATOR = ", ";
}
