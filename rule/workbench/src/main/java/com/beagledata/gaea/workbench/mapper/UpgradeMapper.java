package com.beagledata.gaea.workbench.mapper;

import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.AssetsVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* 描述: 升级版本mapper
* @author: 周庚新
* @date: 2021/1/5
*/
@Mapper
public interface UpgradeMapper {

    void createTables();

	@Update("${sql}")
	void updateTableBySql(@Param("sql") String sql);

	@Select("${sql}")
	List<JSONObject> selectSql(@Param("sql") String sql);

    /**
     * @Author yangyongqiang
     * @Description 更新关联的userUuid设置列值
     * @Date 2:32 下午 2021/1/5
     **/
    void updateKnowledgePackageCreatorUuid();

    /**
     * @Author yangyongqiang
     * @Description 根据user_id列查询userUuid设置user_uuid值
     * @Date 10:07 上午 2021/1/6
     **/
	void updateUserRoleUserUuid();

    /**
    * 描述: 更新t_micro 表的 project_uuid 字段
    * @param: []
    * @author: 周庚新
    * @date: 2021/1/6
    * @return: void
    */
	void updateMicroProjectUuid();

	void updateProjectUserProjectUuid();

	void updateProjectUserUserUuid();

	void updateUserOrgUuid();

	/**
	* 描述: 更新 t_user_permission 表 user_uuid 字段
	* @param: []
	* @author: 周庚新
	* @date: 2021/1/6
	* @return: void
	*/
	void updateUserUuidInUserPermission();

	/**
	* 描述: 根据assets 自身条件查询列表 size = 0 时 为全部查询，size > 0 分页查询
	* @param: [assets, start, size]
	* @author: 周庚新
	* @date: 2021/1/6
	* @return: java.util.List<com.beagledata.gaea.workbench.entity.Assets>
	*/
	List<Assets> selectAssets(@Param("assets") Assets assets, @Param("start")int start,@Param("size") int size);

	void updateAssetsById(Assets assets);

	List<AssetsVersion> selectAssetsVersion(@Param("assets") Assets assets, @Param("start")int start,@Param("size") int size);

	void updateAssetsVersionById(AssetsVersion assets);

	/**
	* 描述: 规则新版本 true 代表保存决策流  false 代表保存 向导式，决策表，决策树，评分卡
	* @param: [ruleFlow]
	* @author: 周庚新
	* @date: 2021/1/7 
	* @return: void
	*/
	void saveRuleNewVersion(@Param("ruleFlow") Boolean ruleFlow);

	/**
     * 旧版有服务，但是服务对应的知识包被删除过，新生成的知识包没有micro_uuid的，
	 * 	将t_micro和t_knowledge_package表根据name和project_uuid关联，在t_knowledge_package表补充micro_uuid,
	 * 	在t_micro表更新package_uuid
     * @author yinrj
     * @date 2021/1/13
     */
	void updateKnowledgePackageWithoutMicroUuid();

	/**
     * 更新t_knowledge_package_assets表的assets_version字段
     * @author yinrj
     * @date 2021/1/7
     */
	void updateKnowledgePackageAssetsVersions();

	void updateVersionNo(@Param("start")int start, @Param("end")int end);

	Integer maxVersionId();

	/**
     *  批量新增t_knowledge_package_baseline表数据
     * @author yinrj
     * @date 2021/1/8
     */
	void insertKnowledgePackageBaseline();

	/**
     *  批量更新t_knowledge_package_baseline表数据
     * @author yinrj
     * @date 2021/1/8
     */
	void updateKnowledgePackageBaseline();

	/**
     *  根据t_project的is_deleted批量更新t_micro表的is_deleted字段
     * @author yinrj
     * @date 2021/1/13
     */
	void updateMicroDeleteTag();

	/**
     *  批量更新t_micro_deployment表数据
     * @author yinrj
     * @date 2021/1/11
     */
	void insertMicroDeployment();

	/**
     *  批量更新t_micro_deployment_model表数据
     * @author yinrj
     * @date 2021/1/11
     */
	void insertMicroDeploymentModel();

	List<Assets> selectReferAssets(@Param("start")int start,@Param("size") int size);

	List<Assets> selectVersionReferAssets(@Param("start")int start,@Param("size") int size);
}
