package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.AssetsVersion;
import com.beagledata.gaea.workbench.vo.AssetsReferenceVO;
import com.beagledata.gaea.workbench.vo.AssetsTreeNodeVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Auther: yinrj
 * @Date: 0018 2018/9/18 17:15
 * @Description: 资源文件管理
 */
@Mapper
public interface AssetsMapper {

	/**
	 * 条件查询资源文件列表
	 * @param params 查询参数
	 */
	List<Assets> selectByParams(Map params);

	/**
	 * 查询资源文件列表总数
	 * @param params 查询参数
	 */
	int selectCountByParams(Map params);

	/**
	 * @auto: yangyongqiang
	 * @Description: 创建资源文件
	 * @Date: 2018-09-18 17:36
	 **/
	int insert(Assets assets);

	/**
	 * @auto: yangyongqiang
	 * @Description: 修改资源文件
	 * @Date: 2018-09-18 17:36
	 **/
	int update(Assets assets);

	/**
	 * 文件版本切换
	 * @param uuid 文件uuid
	 * @param nowVersion 版本号
	 * @author chenyafeng
	 * @date 2018/11/1
	 */
	int updateNowVersion(@Param("uuid") String uuid, @Param("nowVersion") Integer nowVersion);

	/**
	 * @auto: yangyongqiang
	 * @Description: 删除资源文件
	 * @Date: 2018-09-18 17:36
	 **/
	int deleteByUuid(String uuid);

	/**
	 * @auto: yangyongqiang
	 * @Description: 删查询单条资源文件
	 * @Date: 2018-09-18 17:36
	 **/
	Assets selectByUuid(String uuid);

	/**
	 * 根据id查询
	 */
	Assets selectById(int id);

	/**
	 * 根据id和版本查询
	 */
	Assets selectByIdAndVersion(@Param("id") int id, @Param("versionNo") Integer versionNo);

	/**
	 * @author yinrj
	 * @params projectUuid
	 * @Description 根据项目uuid查询资源文件类别集合
	 * @date 0021 2018/9/21 17:41
	 */
	Set<String> selectTypeGroupByProjectUuid(@Param("projectUuid") String projectUuid,
											 @Param("isAdmin") boolean isAdmin,
											 @Param("testCase") String testCase
	);

	/**
	 * @author yinrj
	 * @params [projectUuid, tag]
	 * @Description 根据项目uuid和标签查询资源文件列表
	 * @date 0021 2018/9/21 17:41
	 */
	List<Assets> selectByProjectUuid(@Param("projectUuid") String projectUuid,
									 @Param("isAdmin") boolean isAdmin);

	/**
	 * @author liulu
	 * 2018/9/29 下午 03:55
	 */
	List<Assets> selectBomByProjectUuid(@Param("projectUuid") String projectUuid);

	/**
	 * @author liulu
	 * 2018/10/8 下午 06:05
	 */
	List<Assets> selectByIds(Collection<Integer> ids);

	/**
	 * @author liulu
	 * 2018/10/8 下午 06:05
	 */
	List<Assets> selectByUuids(Collection<String> uuids);

	/**
	 * 描述: 统计个数
	 * @param: [uuids]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: int
	 */
	int selectAssetsCountByUuids(Collection<String> uuids);

	/**
	 * 查询文件锁定的相关属性
	 * @author chenyafeng
	 * @date 2018/11/6
	 */
	Assets selectLockInfo(@Param("uuid") String uuid);

	/**
	 * 更改锁定状态
	 * @author chenyafeng
	 * @date 2018/11/7
	 */
	int updateLock(@Param("uuid") String uuid, @Param("isLock") Boolean isLock,
				   @Param("locker") String locker, @Param("lockTime") Date lockTime);

	/**
	 * 判断文件是否被锁定
	 * @author chenyafeng
	 * @date 2018/11/8
	 */
	Boolean assetsIsLocked(@Param("uuid") String uuid);

	/**
	 * @Author: mahongfei
	 * @description: 获取指定目录下的文件
	 */
	List<Assets> selectAssertByParentId(String dirParentId, String projectUuid, String type,
										boolean isAdmin);

	/**
	 * @Author: mahongfei
	 * @description: 删除文件夹中的文件
	 */
	int deleteByDirParentId(String dirParentId);

	/**
	 * @Author: mahongfei
	 * @description: 查找文件
	 */
	List<Assets> selectByParentId(String dirParentId);

	/**
	 * @Author: mahongfei
	 * @description: 根据名字查找资源文件
	 */
	Assets selectAsset(@Param("name") String name,
					   @Param("type") String type,
					   @Param("projectUuid") String projectUuid,
					   @Param("nowVersion") Integer nowVersion);

	/**
	 * @Author: mahongfei
	 * @description: 根据文件名称和文件夹名称模糊查询文件
	 */
	List<AssetsReferenceVO> selectByAssetNameAndFolderName(@Param("projectUuid") String uuid,
														   @Param("name") String name,
														   @Param("isAdmin") boolean isAdmin);

	/**
	 * @param projectUuid
	 * @return 项目下所有资源文件
	 */
	List<Assets> selectByProject(String projectUuid);

	/**
	 * 批量插入，同时插入t_assets_version
	 * @param assetsList
	 * @return
	 */
	int insertBatch(List<Assets> assetsList);

	/**
	 * 描述: 批量插入版本文件
	 * @param: [assetsList]
	 * @author: 周庚新
	 * @date: 2020/7/23
	 * @return: int
	 */
	int insertVersionBatch(List<AssetsVersion> assetsList);

	/**
	 * 更改正在编辑的编辑人信息
	 * @author yinrj
	 * @date 2020/4/29
	 */
	int updateEditor(@Param("assetsUuids") String[] assetsUuids, @Param("editor") String editor, @Param("lock") Boolean lock);

	/**
	 * 查询当前编辑人信息
	 * uuid 文件uuid
	 */
	Assets selectEditor(@Param("uuid") String uuid);

	/**
	 * 插入新版本记录
	 * @param assets
	 * @return
	 */
	int insertNewVersion(Assets assets);

	/**
	 * 统计资源文件的版本数量
	 * @param assetsUuid
	 * @return
	 */
	int countVersions(String assetsUuid);

	/**
	 * 获取资源文件的版本列表
	 * @param assetsUuid
	 * @return
	 */
	List<Assets> selectVersions(String assetsUuid, int start, int limit);

	/**
	 * 删除版本
	 * @param assets
	 * @return
	 */
	int deleteVersion(Assets assets);

	/**
	 * 切换版本
	 * @param assets
	 * @return
	 */
	int updateFromVersion(Assets assets);

	/**
	 * 查询某个版本的资源文件
	 * @param assetsUuid
	 * @param versionNo
	 * @return
	 */
	Assets selectVersion(String assetsUuid, int versionNo);

	/**
	 * 根据ProjectUuid查询资源文件
	 * @param projectUuid
	 * @return
	 */
	List<Assets> selectAssetsByProjectUuid(@Param("projectUuid") String projectUuid, @Param("types") List<String> types);


	List<AssetsTreeNodeVO> getAssetsTreeNode(@Param("projectUuid") String projectUuid, @Param("parentUuid") String parentUuid, @Param("type") String type);

	/**
	 * 描述: 决策流获取相关版本资源文件的content
	 * @param: [ruleMaps]
	 * @author: 周庚新
	 * @date: 2020/7/1
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.Assets>
	 */
	List<Assets> selectByUuidsAndVersion(List<Assets> rules);


	/**
	 * @Author yangyongqiang
	 * @Description 查询指定项目下的资源版本
	 * @Date 6:47 下午 2020/7/23
	 **/
	List<AssetsVersion> selectVersionsByProjectUuid(@Param("projectUuid") String projectUuid);

	/**
	 * 描述: 根据uuid 查询所有版本文件
	 * @param: [uuids]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.AssetsVersion>
	 */
	List<AssetsVersion> selectVersionsByUuids(Collection<String> uuids);

	/**
	 * 描述: 根据文件夹uuid 查询资源文件
	 * @param: [deletedFolderUuids]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.AssetsVersion>
	 */
	List<Assets> selectByParentIds(Collection<String> deletedFolderUuids);

	/**
	 * 描述: 根据uuid删除资源文件
	 * @param: [deleteAssetsUuids]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: void
	 */
	void deleteByUuids(List<String> deleteAssetsUuids);

	/**
	 * 描述: 根据id 查询版本
	 * @param: [id]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: com.beagledata.gaea.workbench.entity.Assets
	 */
	Assets selectVersionById(Integer id);

	/**
	 * 描述: 根据英文名称和项目uuid 查询资源文件
	 * @param: [enName, projectUuid]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: com.beagledata.gaea.workbench.entity.Assets
	 */
	Assets selectAssetsByEnName(@Param("enName") String enName, @Param("projectUuid") String projectUuid);

	/**
	 * 描述: 导入基线查询是否存在
	 * @param: [aiModel]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: com.beagledata.gaea.workbench.entity.AiModel
	 */
	Assets selectOnUploadBaseline(Assets assets);

	/**
	 * 描述: 导入基线，更新
	 * @param: [aiModel]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: void
	 */
	void updateOnUploadBaseline(Assets assets);

	/**
	 * 描述: 导入基线查询是否存在
	 * @param: [aiModel]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: com.beagledata.gaea.workbench.entity.AiModel
	 */
	AssetsVersion selectVersionOnUploadBaseline(AssetsVersion assetsVersion);

	/**
	 * 上传基线插入资源版本
	 * @param assetsVersion
	 */
	void insertVersion(AssetsVersion assetsVersion);

	/**
	 * 描述: 导入基线，更新
	 * @param: [aiModel]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: void
	 */
	void updateVersionOnUploadBaseline(AssetsVersion assetsVersion);

	/**
	 * Description: 根据常量名称和项目uuid查询常量
	 * @param name: 常量名称
	 * @param projectUuid:  项目uuid
	 * @return: com.beagledata.gaea.workbench.entity.Assets
	 * @author: 周庚新
	 * @date: 2021/1/20
	 */
	Assets selectConstantByNameAndProject(@Param("name")String name, @Param("projectUuid")String projectUuid);

	Assets selectFactAssetsByName(@Param("name")String name, @Param("projectUuid")String projectUuid);

	/**
	 * 统计项目中文件的数量
	 * 返回格式: {"fact": {"num":2,"type":"fact"}, "constant": {"num":6,"type":"constant"}}
	 */
	@MapKey("type")
	Map<String, Map<String, Object>> countNumByTypeForPrj(@Param("projectUuid")String projectUuid);
}
