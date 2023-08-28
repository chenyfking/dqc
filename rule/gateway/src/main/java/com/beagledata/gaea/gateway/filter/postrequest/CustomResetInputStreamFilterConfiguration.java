package com.beagledata.gaea.gateway.filter.postrequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 描述:
 * 自定义重置输入流配置
 *
 * @author 周庚新
 * @date 2020-11-06
 */
@Configuration
public class CustomResetInputStreamFilterConfiguration {
	@Bean
	public CustomResetInputStreamFilter customResetInputStreamFilter(){
		return new CustomResetInputStreamFilter();
	}
}