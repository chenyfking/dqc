package com.beagledata.gaea.workbench.service;

import com.beagledata.gaea.workbench.entity.Client;

/**
 * Created by liulu on 2020/5/13.
 */
public interface RestApiService {
    /**
     * 注册集群节点
     *
     * @param client
     */
    void registerClient(Client client);

    /**
     * 跑批远程调用获取服务zip包
     * @author yinrj
     * @param microUuid
     * @date 2020/7/23
     */
    byte[] getMicroPackage(String microUuid);
}
