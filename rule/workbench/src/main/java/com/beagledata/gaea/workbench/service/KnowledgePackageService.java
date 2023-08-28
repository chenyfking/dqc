package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.KnowledgePackage;
import com.beagledata.gaea.workbench.rule.define.Fact;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mahongfei on 2018/10/8.
 */
public interface KnowledgePackageService {
     /**
     *@Author: mahongfei
     *@description: 添加知识包
     */
    void addKnowledgePackage(KnowledgePackage knowledgePackage);

     /**
     *@Author: mahongfei
     *@description: 编辑知识包
     */
    void editKnowledgePackage(KnowledgePackage knowledgePackage);

    /**
     *@Author: mahongfei
     *@description: 知识包列表
     */
    List<KnowledgePackage> listAll(String projectUuid);

    /**
     * 查看项目的知识包列表(无需登录)
     */
    List<KnowledgePackage> getPkgList(String projectUuid);

    /**
     *@Author: mahongfei
     *@description: 删除知识包
     */
    Result deleteknowledgePackage(String uuid);

    /**
     *@Description:增加知识包
     **/
    void addKnowledgePackageAssets(KnowledgePackage knowledgePackage);

    /**
     *@Description:查找知识包资源
     **/
    List<Assets> selectKnowledgePackageAssets(KnowledgePackage knowledgePackage);

    /**
     *@Description:删除知识包资源
     **/
    void deleteKnowledgePackageAssets(KnowledgePackage knowledgePackage);
    
    /**
     * @author liulu
     * 2018/10/8 下午 05:40
     */
    List<Fact> getTestData(String uuid, Integer baselineVersion);

    /**
     * @author liulu
     * 2018/10/9 下午 04:14
     */
    Map test(String uuid, String json, Integer baselineVersion);

    /**
     * 解析知识包关联的文件，得到文件使用的Fact对象的id集合
     */
    Set<Integer> getknowledgePackageFactIds(List<Assets> assetsList);

    /**
     * @author liulu
     * 2018/10/10 下午 05:42
     */
    void download(String uuid,Integer baselineVersion, HttpServletResponse response);

    /**
     * 下载最新基线的知识包
     * uuid: 知识包uuid
     */
    void download(String uuid, HttpServletResponse response);

    /**
     * 上传基线
     *
     * @param file
     * @return
     */
    Result upload(MultipartFile file);

    /**
     * 下载批量测试的模板
     * @author chenyafeng
     * @date 2018/11/7
     */
    void downBatchModel(String uuid, Integer baselineVersion, HttpServletResponse response);

    /**
     * 上传批量测试数据
     * @author chenyafeng
     * @date 2018/11/16
     */
    Result importBatchData(String uuid, MultipartFile file);

    /**
     * 批量测试，直接返回测试结果文件
     *
     * @param uuid
     * @param baselineVersion
     * @param file
     * @param response
     */
    void batchTest(String uuid, Integer baselineVersion, MultipartFile file, HttpServletResponse response);

    /**
     * 知识包提交审核
     * @author chenyafeng
     * @date 2018/12/4
     */
    Result submitAudit(String packageUuid);

    /**
     * 创建临时知识包
     *
     * @author liulu
     * 2019/5/21 16:53
     */
    KnowledgePackage addTempPkg(String assetsUuid,Integer assetsVersion);

    /**
     * 下周批量测试的结果数据
     * Created by Chenyafeng on 2020/3/23
     */
    void exportBatchResult(HttpServletResponse response, String json);

    /**
     * 下载知识包相关数据模型的信息
     * @param pkguuid
     */
    void downPkgFacts(String pkguuid, HttpServletResponse response);
}
