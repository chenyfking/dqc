package com.beagledata.gaea.gateway.filter.postrequest;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.niws.client.http.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-11-06
 */
@Configuration
@ConditionalOnBean(RibbonClientConfiguration.class)
public class RibbonPerClientConfiguration {
	private String name = "client";

	@Bean
	public RestClient ribbonRestClient(IClientConfig config, ILoadBalancer loadBalancer) {
		RestClient restClient = new PostAwareResetClient(config);
		restClient.setLoadBalancer(loadBalancer);
		return restClient;
	}
}