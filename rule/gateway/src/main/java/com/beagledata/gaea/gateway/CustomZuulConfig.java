package com.beagledata.gaea.gateway;

import com.beagledata.gaea.common.RedisConstants;
import com.beagledata.gaea.gateway.filter.TokenFilter;
import com.beagledata.gaea.gateway.mapper.MicroRouteMapper;
import com.beagledata.gaea.gateway.mapper.MicroTokenMapper;
import com.beagledata.gaea.gateway.route.CustomRouteLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by liulu on 2020/6/29.
 */
@Configuration
public class CustomZuulConfig {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ZuulProperties zuulProperties;
	@Autowired
	private ServerProperties serverProperties;
	@Autowired
	private MicroRouteMapper microRouteMapper;
	@Autowired
	private MicroTokenMapper microTokenMapper;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@PostConstruct
	public void init() {
		String ip = getIp();
		if (ip == null) {
			return;
		}

		int port = serverProperties.getPort();
		String contextPath = serverProperties.getContextPath();
		if (!contextPath.startsWith("/")) {
			contextPath = "/" + contextPath;
		}
		String url = String.format("http://%s:%s%s", ip, port, contextPath);
		BoundHashOperations<String, String, String> hashOperations = stringRedisTemplate.boundHashOps("gaea.helth");
		// 本地地址缓存redis，健康检查
		hashOperations.put(String.format("gateway-%s:%s", ip, port), url);
		logger.info("本机地址：{}", url);
	}

	@Bean
	public CustomRouteLocator routeLocator() {
		CustomRouteLocator routeLocator = new CustomRouteLocator(serverProperties.getServletPrefix(), zuulProperties);
		routeLocator.setMicroRouteMapper(microRouteMapper);
		return routeLocator;
	}

	@Bean
	public TokenFilter tokenFilter() {
		TokenFilter filter = new TokenFilter();
		filter.setMicroTokenMapper(microTokenMapper);
		return filter;
	}

	@Bean
	public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
												   MessageListenerAdapter listenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		// 订阅刷新消息
		container.addMessageListener(listenerAdapter, new PatternTopic(RedisConstants.Gateway.CHANNEL_REFRESH_KEY));
		return container;
	}

	@Bean
	public MessageListenerAdapter listenerAdapter(RedisRefreshListener redisRefreshListener) {
		return new MessageListenerAdapter(redisRefreshListener, "refresh");
	}

	@Bean
	public RedisRefreshListener redisRefreshListener() {
		return new RedisRefreshListener();
	}

	class RedisRefreshListener {
		@Autowired
		private ApplicationEventPublisher publisher;
		@Autowired
		private RouteLocator routeLocator;

		public void refresh(String message) {
			if (RedisConstants.Gateway.CHANNEL_REFRESH_TOKEN.equals(message)) {
				publisher.publishEvent(new TokenFilter.TokensRefreshedEvent(message));
			} else if (RedisConstants.Gateway.CHANNEL_REFRESH_ROUTE.equals(message)) {
				publisher.publishEvent(new RoutesRefreshedEvent(routeLocator));
			}
		}
	}

	private String getIp() {
		try {
			Enumeration<NetworkInterface> allInterFaces = NetworkInterface.getNetworkInterfaces();
			while (allInterFaces.hasMoreElements()) {
				NetworkInterface ni = allInterFaces.nextElement();
				if (ni.isLoopback() || ni.isVirtual() || !ni.isUp()) {
					continue;
				}

				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress ip = addrs.nextElement();
					if (ip != null && ip instanceof Inet4Address) {
						return ip.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			logger.error("获取本机IP失败", e);
		}
		return null;
	}
}
