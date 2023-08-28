package com.beagledata.gaea.common.task;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.Protocol;
import redis.clients.util.SafeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 描述:
 * redis实现任务锁
 *
 * @author 周庚新
 * @date 2020-09-18
 */
public class RedisTaskLocker implements TaskLocker {

	/**
	* 描述: 锁超时时间，单位分
	*/
	private static final int TIMEOUT = 10;

	private RedisTemplate redisTemplate;
	/**
	 * 描述: 名称
	 */
	private String name;
	/**
	 * 描述: key
	 */
	private String key;

	public RedisTaskLocker(String name, RedisTemplate redisTemplate) {
		this.name = name;
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 描述: 获取锁
	 *
	 * @param: []
	 * @author: 周庚新
	 * @date: 2020/9/18
	 * @return: boolean
	 */
	@Override
	public boolean lock() {
		/*
		* 使用redis set 命令实现分布式锁
		*/
		return (Boolean) redisTemplate.execute(new RedisCallback<Boolean>(){
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				RedisSerializer keySerializer = redisTemplate.getKeySerializer();
				RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
				// 每天设置一个key，避免死锁
				key = name + "_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
				Object obj = connection.execute("set",
						keySerializer.serialize(key),
						valueSerializer.serialize("1"),
						SafeEncoder.encode("NX"),
						SafeEncoder.encode("EX"),
						Protocol.toByteArray(
								TimeUnit.MINUTES.toSeconds(TIMEOUT)));
				return obj !=null;
				
			}
		});
	}

	/**
	 * 描述: 释放锁
	 *
	 * @param: []
	 * @author: 周庚新
	 * @date: 2020/9/18
	 * @return: void
	 */
	@Override
	public void unlock() {
		redisTemplate.delete(key);
	}
}