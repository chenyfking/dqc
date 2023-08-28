package com.beagledata.gaea.gateway.loadbalance;

import com.beagledata.gaea.gateway.entity.MicroRoute;
import com.beagledata.gaea.gateway.mapper.MicroRouteMapper;
import com.netflix.client.IClientConfigAware;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-11-06
 */
public class CustomServerList implements ServerList<Server>, IClientConfigAware {
	private IClientConfig iClientConfig;
	private MicroRouteMapper microRouteMapper;

	@Override
	public void initWithNiwsConfig(IClientConfig iClientConfig) {
		this.iClientConfig = iClientConfig;
	}

	@Override
	public List<Server> getInitialListOfServers() {
		return getUpdatedListOfServers();
	}

	@Override
	public List<Server> getUpdatedListOfServers() {
		List<MicroRoute> microRoutes = microRouteMapper.selectAllBaseUrl();
		List<Server> serverList = new ArrayList<>(microRoutes.size());
		for (MicroRoute microRoute : microRoutes) {
		    Server server = new Server(microRoute.getUrl());
		    serverList.add(server);
		}
		return serverList;
	}

	public void setMicroRouteMapper(MicroRouteMapper microRouteMapper) {
		this.microRouteMapper = microRouteMapper;
	}
}