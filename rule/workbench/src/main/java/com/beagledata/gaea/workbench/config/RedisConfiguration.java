package com.beagledata.gaea.workbench.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;

/**
 * 描述:
 * @author 周庚新
 * @date 2020-11-11
 */
@Configuration
public class RedisConfiguration {
	@Resource
	private RedisTemplate redisTemplate;

	@Bean
	public RedisTemplate<String, Object> setRedisTemplate() {
		RedisSerializer<String> serializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(serializer);
		redisTemplate.setHashKeySerializer(serializer);
		return redisTemplate;
	}
}