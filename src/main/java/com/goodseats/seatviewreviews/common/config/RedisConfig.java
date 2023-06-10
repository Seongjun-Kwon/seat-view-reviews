package com.goodseats.seatviewreviews.common.config;

import static com.goodseats.seatviewreviews.common.constant.RedisConstant.*;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
	private String redisHost;

	@Value("${spring.redis.port}")
	private int redisPort;

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		config.useSingleServer().setAddress(REDISSON_ADDRESS_PREFIX + redisHost + ":" + redisPort);
		config.setCodec(new StringCodec());
		return Redisson.create(config);
	}
}
