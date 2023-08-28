package com.beagledata.gaea.workbench.service;

/**
 * @Auther: yinrj
 * @Date: 0021 2020/7/21 11:09
 * @Description: 服务路由
 */
public interface MicroRouteService {
    /**
     * 保存服务路由
     * @author yinrj
     * @date 2020/7/21
     */
    void saveMicroRoute(String clientUuid, String microUuid);

    /**
     * 删除服务路由
     *
     * @param clientUuid
     * @param microUuid
     */
    void deleteMicroRoute(String clientUuid, String microUuid);

    /**
     * 刷新服务路由变动提示到Redis
     * @author yinrj
     * @date 2020/7/21
     */
    void refreshMicroRouteToRedis();
}
