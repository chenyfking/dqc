package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.KnowledgePackageBaseline;

import java.util.List;

/**
 * @Auther: yinrj
 * @Date: 0028 2020/6/28 16:54
 * @Description: 知识包基线Service
 */
public interface KnowledgePackageBaselineService {

    /**
     * 新增基线
     * @auther yinrj
     * @date 2020/6/28
     * @param packageUuid
     * @param assets
     */
    Result add(String packageUuid, String assets);

    /**
     * @Author yangyongqiang
     * @Description 删除知识包基线
     * @Date 9:50 上午 2020/7/1
     * @Param [uuid]
     * @return void
     **/
    void delete(String uuid);


    /**
     * 基线列表查询
     * @auther yinrj
     * @date 2020/6/29
     * @param packageUuid
     */
    List<KnowledgePackageBaseline> listByPackageUuid(int page, int pageNum, String packageUuid);

    /**
     * @Author yangyongqiang
     * @Description 决策服务查询
     * @Date 7:26 下午 2020/7/2
     **/
    List<KnowledgePackageBaseline> listMicroBaselineByPackageUuid(int page, int pageNum, String packageUuid);

    /**
     * 基线列表总数
     * @auther yinrj
     * @date 2020/6/29
     * @param packageUuid
     */
    int getCountByPackageUuid(String packageUuid);

    /**
     * @Author yangyongqiang
     * @Description 决策服务查询数量
     * @Date 7:28 下午 2020/7/2
     **/
    int getCountMicroBaselineByPackageUuid(String packageUuid);

    /**
     * 发布基线生成服务
     * @author yinrj
     * @date 2020/7/1
     */
    void publish(String packageUuid, Integer baselineVersion);

    /**
     * @Author yangyongqiang
     * @Date 10:27 上午 2020/7/6
     **/
    void auditBaseline(KnowledgePackageBaseline knowledgePackageBaseline);

	/**
	 * 描述: 基线版本比较
	 * @param: [packageUuid, baselineV1, baselineV2]
	 * @author: 周庚新
	 * @date: 2020/7/6
	 * @return: com.beagledata.common.Result
	 *
	 */
	Result compare(String packageUuid, Integer baselineV1, Integer baselineV2);

    /**
     * @Author yangyongqiang
     * @Description 微服务部署上线用的查询表单接口
     * @Date 6:36 下午 2020/7/16
     **/
    List<KnowledgePackageBaseline> selectListMicroBaseline(String packageUuid);
}
