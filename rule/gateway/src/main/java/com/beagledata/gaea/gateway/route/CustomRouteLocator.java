package com.beagledata.gaea.gateway.route;

import com.beagledata.gaea.gateway.entity.MicroRoute;
import com.beagledata.gaea.gateway.mapper.MicroRouteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 动态服务路由
 *
 * Created by liulu on 2020/6/29.
 */
public class CustomRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private MicroRouteMapper microRouteMapper;

    public CustomRouteLocator(String servletPath, ZuulProperties properties) {
        super(servletPath, properties);
    }

    @Override
    public void refresh() {
        logger.info("刷新路由");
        doRefresh();
    }

    @Override
    protected Map<String, ZuulProperties.ZuulRoute> locateRoutes() {
        Map<String, ZuulProperties.ZuulRoute> routesMap = new LinkedHashMap<>();
        routesMap.putAll(super.locateRoutes());
        for (MicroRoute microRoute : microRouteMapper.selectAll()) {
            String microUuid = microRoute.getMicroUuid();
            logger.info("注册服务{}的相关路由", microUuid);
            //同步执行路由
            ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute();
            zuulRoute.setId("execute");
            zuulRoute.setServiceId("execute-" + microUuid);
            zuulRoute.setPath("/execute/" + microUuid);
            routesMap.put(zuulRoute.getPath(), zuulRoute);

            //异步执行路由
            ZuulProperties.ZuulRoute zuulRouteAsync = new ZuulProperties.ZuulRoute();
            zuulRouteAsync.setId("executeasync");
            zuulRouteAsync.setServiceId("executeasync-" + microUuid);
            zuulRouteAsync.setPath("/executeasync/" + microUuid);
            routesMap.put(zuulRouteAsync.getPath(), zuulRouteAsync);
        }
        //异步查询结果路由
        ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute();
        zuulRoute.setId("query");
        zuulRoute.setServiceId("query");
        zuulRoute.setPath("/query");
        routesMap.put(zuulRoute.getPath(), zuulRoute);
        return routesMap;
    }

    public void setMicroRouteMapper(MicroRouteMapper microRouteMapper) {
        this.microRouteMapper = microRouteMapper;
    }
}
