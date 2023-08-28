package com.beagledata.gaea.common;

import com.beagledata.util.RSAUtils;
import com.beagledata.util.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述:
 * yml 文件加解密
 *
 * @author 周庚新
 * @date 2020-10-14
 */
public class SafelyYmlProcessor implements EnvironmentPostProcessor {
	@Override
	public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication ctx) {
		Map<String, Object> map = new HashMap<>();
		decryptProperty(env, map, "spring.datasource.druid.password");
		decryptProperty(env, map, "spring.redis.password");
		if (!map.isEmpty()) {
		    env.getPropertySources().addFirst(new MapPropertySource("SafelyYml", map));
		}
	}

	private void decryptProperty(ConfigurableEnvironment env, Map<String, Object> map, String name) {
		String value = env.getProperty(name);
		if (StringUtils.isBlank(value)) {
		    return;
		}
		try {
			value = RSAUtils.decrypt(value);
		} catch (Exception e) {
			throw  new IllegalStateException(String.format("YML配置解密失败. name: %s, value: %s", name, value));
		}
		map.put(name, value);
	}
}