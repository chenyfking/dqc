package com.beagledata.gaea.gateway.loadbalance;

import com.beagledata.gaea.gateway.mapper.MicroRouteMapper;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-11-06
 */
public class CustomRibbonClientConfiguration extends RibbonClientConfiguration {

	@Autowired
	MicroRouteMapper microRouteMapper;

	@Bean
	@Override
	public IClientConfig ribbonClientConfig() {
		return super.ribbonClientConfig();
	}

	@Bean
	@Override
	public ServerList<Server> ribbonServerList(IClientConfig config) {
		CustomServerList serverList = new CustomServerList();
		serverList.setMicroRouteMapper(microRouteMapper);
		serverList.initWithNiwsConfig(config);
		return serverList;
	}
}