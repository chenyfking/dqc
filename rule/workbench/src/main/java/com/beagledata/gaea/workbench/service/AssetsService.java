package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.Refer;
import com.beagledata.gaea.workbench.vo.VerifierVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Auther: yinrj
 * @Date: 0018 2018/9/18 17:25
 * @Description:
 */
public interface AssetsService {

    /**
     *条件查询资源文件列表
     * @param page 当前页码
     * @param pageNum 每页行数
     * @param params  查询参数
     */
    List<Assets> listOfParams(int page, int pageNum, Map params);

    /**
     * 查询资源文件列表总数
     * @param params
     */
    int getTotal(Map params);

    /**
     *@auto: yangyongqiang
     *@Description: 创建资源文件
     *@Date: 2018-09-18 17:16
     **/
    void addAssets(Assets assets);

    /**
     *@auto: yangyongqiang
     *@Description: 编辑资源文件
     *@Date: 2018-09-18 17:16
     **/
    void editAssets(Assets assets);

    /**
     *@auto: yinrj
     *@Description: 更新编辑人信息
     *@Date: 2020-04-29 17:16
     **/
    Result updateEditor(String assetsUuids, boolean lock, String userUuid);

    /**
     *@auto: yangyongqiang
     *@Description: 保存资源文件
     *@Date: 2018-09-18 17:16
     **/
    Result saveContent(Assets assets);

    /**
     *@auto: yangyongqiang
     *@Description: 删除资源文件
     *@Date: 2018-09-18 17:16
     **/
    Result delete(String uuid);

    /**
     *@auto: yangyongqiang
     *@Description: 资源文件详情
     *@Date: 2018-09-18 17:16
     **/
    Result selectAssets(String uuid, int versionNo);

    /**
     * 根据id查询文件内容
     */
    Result selectContentById(int id, Integer versionNo);

    /**
     * @author yinrj
     * @params projectUuid
     * @Description 根据项目uuid查询资源文件类别集合
     * @date 0021 2018/9/21 17:41
     */
    Set<String> listTypeGroupByProjectUuid(String projectUuid);

    /**
     * @author yinrj
     * @params [projectUuid, tag]
     * @Description 根据项目uuid和标签查询资源文件列表
     * @date 0021 2018/9/21 17:41
     */
    List<Assets> listByProjectUuid(String projectUuid);

    /**
     * @author liulu
     * 2018/9/29 下午 03:58
     */
    Map listBomByProjectUuid(String projectUuid);

    /**
     * 锁定/解锁文件
     * @author chenyafeng
     * @date 2018/11/6
     * @param uuid 文件的uuid
     * @param isLock true:锁定 false:解锁
     */
    Result lockAssets(String uuid, Boolean isLock);

    /**
     * @Author: mahongfei
     * @description: 根据文件名称和文件夹名称模糊查询文件
     */
    Result listOfAssetNameAndFolderName(Assets assets);

    /**
     * 获得文件当前版本和目标版本的content
     * Created by Chenyafeng on 2019/7/24
     */
    Result getCompareContent(String assetUuid, String assetType, Integer baseVersion, Integer compareVersion);

    /**
     * 保存新版本
     *
     * @param assets
     */
    void addNewVersion(Assets assets);

    /**
     * 获取资源文件的版本列表
     *
     * @param assetsUuid
     * @return
     */
    Result listVersionsByUuid(String assetsUuid, int page, int pageNum);

    /**
     * 删除版本
     *
     * @param assets
     */
    Result deleteVersion(Assets assets);

    /**
     * 切换版本
     *
     * @param assets
     */
    Result changeVersion(Assets assets);

    /**
     * 规则复制
     * @param assets 规则文件
     * @return
     */
    Assets rulesCopy(Assets assets);

    /**
     * @auto: yinrj
     *@description: 导入自定义.java或.class或.jar格式模型
     *@date: 2020/6/8 16:49
     */
    Result importJavaFile(String projectUuid, MultipartFile file);

    Result searchVariablesOrRules(String projectUuid, String keyword, String type);

    /**
    * 描述: 根据节点类型和父节点uuid 获取数的子节点
    * @param: [parentUuid, type]
    * @author: 周庚新
    * @date: 2020/6/22 
    * @return: com.beagledata.common.Result
    * 
    */
    Result getAssetsTreeNode(String projectUuid, String parentUuid, String type);

    /**
    * 描述: 获取某一个分类下边的 文件夹树 （只有文件夹）
    * @param: [projectUuid, type]
    * @author: 周庚新
    * @date: 2020/6/24
    * @return: com.beagledata.common.Result
    *
    */
    Result getFolderTree(String projectUuid, String type);

    /**
    * 描述: 规则校验
    * @param verifierVO
     * @return
    */
    Result verifier(VerifierVO verifierVO);

    /**
     *
     * 资源文件导出
     * @param uuid
     * @param response
     * @return
     */
    void export(String uuid, HttpServletResponse response);

    /**
     * 资源文件导入
     *
     * @param projectUuid
     * @param file
     * @return
     */
    Result importRule(String projectUuid, MultipartFile file);

    /**
     * 获取资源文件的引用关系
     *
     * @param assets
     * @return
     */
    Set<Refer> getRefers(Assets assets);

    /**
     * 统计某个项目的各种文件的数量
     * projectUuid: 项目uuid
     * types: 要统计的文件类型, 只返回types中包含类型的文件数量
     */
    Map<String, Integer> countNumByTypeForPrj(String projectUuid, Set<String> types);

    /**
     * 自动创建指定项目中的数据模型
     */
    Result autoCreateFact(String json);
}
