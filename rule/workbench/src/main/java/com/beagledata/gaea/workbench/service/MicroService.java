package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.KnowledgePackage;
import com.beagledata.gaea.workbench.entity.KnowledgePackageBaseline;
import com.beagledata.gaea.workbench.entity.Micro;

import java.util.Date;
import java.util.List;

/**
 * 服务相关
 * Created by Chenyafeng on 2018/12/3.
 */
public interface MicroService {
    /**
     * 查询服务
     * @author chenyafeng
     * @date 2018/12/4
     */
    Result listPage(Micro micro, int page, int pageNum);

    /**
     * 知识包审核未通过
     * @author chenyafeng
     * @date 2018/12/5
     */
    Result refusePass(KnowledgePackage knowledgePackage);

    /**
     * 启用/停用服务
     * @author chenyafeng
     * @date 2018/12/4
     */
    Result enableMicro(String uuid, Boolean enable);

    /**
     * 修改服务
     * @author chenyafeng
     * @date 2018/12/3
     */
    Result updateMicro(Micro micro);

    /**
     * 删除服务
     */
    void deleteMicro(String uuid);

    /**
     * 获取服务API调用文档
     *
     * @param microUuid
     * @return
     */
    Result getApiDoc(String microUuid);

    /**
     * 分页获取服务调用记录
     *
     * @param microUuid
     * @return
     */
    Result listPageExecuteRecords(String microUuid, int page, int pageNum, Date startTime, Date endTime);

    Result approvalData(String uuid);

    /**
     * 查询可以上线的基线列表
     *
     * @author liulu
     * 2020/11/17 14:29
     */
    List<KnowledgePackageBaseline> listBaselineForDeploy(String microUuid);

    /**
     * @author liulu
     * 2020/11/18 9:45
     */
    List<Micro> listAllForToken();
}
