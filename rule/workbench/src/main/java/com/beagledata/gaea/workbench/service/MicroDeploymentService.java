package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.MicroDeployment;

import java.util.List;

/**
 * @Author yangyongqiang
 * 服务模型优化
 **/
public interface MicroDeploymentService {
    /**
     * 模型上线部署（A/B测试及冠军挑战者测试）
     * @author yinrj
     * @date 2020/7/14
     */
    Result deploy(MicroDeployment microDeployment);
    /**
     * @Author yangyongqiang
     * @Description 查询服务上线列表
     * @Date 4:15 下午 2020/7/14
     **/
    Result listPage(int page, int pageNum, String microUuid);

    /**
     * @Author yinrj
     * @Description 查询所有生效服务
     * @Date 2021/1/11
     **/
    List<MicroDeployment> listAll();

    /**
     * 下线服务
     *
     * @param microUuid
     * @param packageUuid
     */
    void offline(String microUuid, String packageUuid);

    /**
     * 根据上线记录获取服务包
     *
     * @param deployment
     * @return
     * @throws Exception
     */
    byte[] getMicroPackage(MicroDeployment deployment) throws Exception;
}
