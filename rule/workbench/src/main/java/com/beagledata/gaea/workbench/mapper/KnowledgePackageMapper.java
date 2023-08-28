package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.KnowledgePackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by mahongfei on 2018/10/8.
 */
@Mapper
public interface KnowledgePackageMapper {
	/**
	 * @Author: mahongfei
	 * @description: 添加知识包
	 */
	int insert(KnowledgePackage knowledgePackage);

	/**
	 * @Author: mahongfei
	 * @description: 编辑知识包
	 */
	int update(KnowledgePackage knowledgePackage);

	/**
	 * 描述: 还原已经删除的知识包
	 * @param: [knowledgePackage]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: int
	 */
	int updateDeleted(KnowledgePackage knowledgePackage);

	/**
	 * @Author: mahongfei
	 * @description: 知识包列表
	 */
	List<KnowledgePackage> selectAll(@Param("projectUuid") String ProjectUuid,
									 @Param("isAdmin") boolean isAdmin);

	/**
	 * 查询项目中的所有知识包
	 * @param projectUuid 项目uuid
	 */
	List<KnowledgePackage> selectForPrj(@Param("projectUuid") String projectUuid);

	/**
	 * @Author: mahongfei
	 * @description: 删除知识包
	 */
	int delete(KnowledgePackage knowledgePackage);

	/**
	 * @Description:增加知识包资源
	 **/
	int addKnowledgePackageAssets(KnowledgePackage knowledgePackage);

	/**
	 * @Description:查找知识包资源
	 **/
	List<Assets> selectKnowledgePackageAssets(KnowledgePackage knowledgePackage);

	/**
	 * @Description:删除知识包资源
	 **/
	int deleteKnowledgePackageAssets(KnowledgePackage knowledgePackage);

	/**
	 * @author liulu
	 * 2018/10/8 下午 05:38
	 */
	List<Assets> selectAssetsWithContent(String uuid);

	/**
	 * @author liulu
	 * 2018/10/10 下午 06:34
	 */
	KnowledgePackage selectByUuid(String uuid);

	/**
	 * 查询知识包及最新的基线版本号
	 */
	KnowledgePackage selectForRecentBlByUuid(String uuid);

	/**
	 * @author liulu
	 * 2019/5/21 17:00
	 */
	KnowledgePackage selectTempByAssets(@Param("assetsUuid") String assetsUuid, @Param("assetsVersion") Integer assetsVersion);

	/**
	 * 批量插入，同时插入关联资源文件uuid
	 * @param pkgs
	 * @return
	 */
	int insertBatch(List<KnowledgePackage> pkgs);

	List<Assets> selectAssetsVersionWithContent(@Param("uuid") String uuid, @Param("baselineVersion") Integer baselineVersion);

	/**
	 * @Author yangyongqiang
	 * @Description 更具项目uuid查询知识包资源文件
	 * @Date 7:48 下午 2020/7/23
	 **/
	List<KnowledgePackage> selectPkgAssetsByProjectUuid(@Param("projectUuid") String projectUuid);

	/**
	 * 描述: 统计知识包生效的基线版本个数
	 * @param: [uuid]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.lang.Integer
	 */
	Integer countPkgEffectiveBaseline(String uuid);

	/**
	 * 描述: 查询资源被引用的的知识包列表
	 * @param: [assetsUuid]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.KnowledgePackage>
	 */
	List<KnowledgePackage> selectReferenceKnowledgePackages(@Param("assetsUuid") String assetsUuid);

	/**
	* 描述: 根据知识包名称和项目uuid 查询
	* @param: [name, projectUuid]
	* @author: 周庚新
	* @date: 2020/11/11 
	* @return: com.beagledata.gaea.workbench.entity.KnowledgePackage
	*/
	KnowledgePackage selectByNameAndProjectUuid(@Param("name") String name, @Param("projectUuid") String projectUuid);
	/**
	* 描述: 根据知识包uuid查询
	* @param: [packageUuid]
	* @author: 周庚新
	* @date: 2020/11/11 
	* @return: com.beagledata.gaea.workbench.entity.KnowledgePackage
	*/
	KnowledgePackage selectDeletedByPackageUuid(@Param("packageUuid") String packageUuid);

	/**
	* 描述: 根据知识包uuid 查询项目uuid
	* @param: [packageUuid]
	* @author: 周庚新
	* @date: 2020/11/11 
	* @return: java.lang.String
	*/
	String selectProjectUuidByPkgUuid(@Param("packageUuid")String packageUuid);

    /**
     * 描述: 导入基线查询是否存在
     * @param: [aiModel]
     * @author: 周庚新
     * @date: 2020/11/11
     * @return: com.beagledata.gaea.workbench.entity.AiModel
     */
    KnowledgePackage selectOnUploadBaseline(KnowledgePackage knowledgePackage);

    /**
     * 描述: 导入基线，
     * @param: [aiModel]
     * @author: 周庚新
     * @date: 2020/11/11
     * @return: void
     */
    void updateOnUploadBaseline(KnowledgePackage knowledgePackage);

}
