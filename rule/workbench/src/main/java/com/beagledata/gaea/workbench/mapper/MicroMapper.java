package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.KnowledgePackageBaseline;
import com.beagledata.gaea.workbench.entity.Micro;
import com.beagledata.gaea.workbench.entity.MicroType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务
 * @author chenyafeng
 * @date 2018/12/3
 */
@Mapper
public interface MicroMapper {
	/**
	 * 新增
	 * @param micro
     */
	void insert(Micro micro);

	/**
	 * 根据uuid查询
	 */
	Micro selectByUuid(String uuid);

	/**
	 * 多条件查询服务列表，不含调用记录
	 */
	List<Micro> selectPage(
			@Param("micro") Micro micro,
			@Param("isAdmin") boolean isAdmin,
			@Param("isOrgAdmin") boolean isOrgAdmin,
			@Param("userUuid") String userUuid,
			@Param("start") Integer start,
			@Param("limit") Integer limit
	);

	/**
	 * 多条件查询服务列表，不含调用记录
	 */
	List<Micro> selectAll(
			@Param("isAdmin") boolean isAdmin,
			@Param("isOrgAdmin") boolean isOrgAdmin,
			@Param("userUuid") String userUuid
	);

	/**
	 * 统计服务列表数量
	 */
	int countTotal(
			@Param("micro") Micro micro,
			@Param("isAdmin") boolean isAdmin,
			@Param("isOrgAdmin") boolean isOrgAdmin,
			@Param("userUuid") String userUuid
	);

	/**
	 * 修改可用、不可用
	 */
	void updateEnable(@Param("uuid")String uuid,
					  @Param("enable")boolean enable);

	/**
	 * 删除
	 */
	int delete(String uuid);

	/**
	 * 更新
	 */
	void update(Micro micro);

	/**
	 * 还原删除的服务
	 *
	 * @param packageUuid
	 */
	void updateDeleted(@Param("packageUuid") String packageUuid);

	/**
	 * 新增服务类别
	 */
	void insertType(MicroType microType);

	/**
	 * 删除服务分类
	 */
	void deleteType(@Param("uuid") String uuid, @Param("microTypeModel") String microTypeModel);

	/**
	 * 查询服务是否重复创建
	 *yangyongqiang
	 *@Date: 2019-01-24 18:38
	 **/
    int countByPackageUuid(@Param("packageUuid") String packageUuid);

	/**
	  *@auto: yangyongqiang
	  *@Description 检查服务名称创建是否重复
	  *@Date: 2019-02-25 17:13
	  **/
	int selectMicroName(@Param("name")String name);

	/**
	 * @Author: mahongfei
	 * @description: 根据ai模型的uuid（packageUuid）查找服务
	 */
	Micro selectMicroByPackageUuid(String packageUuid);

	/**
	 * 查询AI模型关联的服务列表
	 *
	 * @param modelUuid
	 * @return
	 */
	List<Micro> selectByModel(String modelUuid);

	/**
	 * 查询集群节点上正在运行的服务
	 *
	 * @param clientUuid
	 * @return
	 */
	List<Micro> selectByClient(String clientUuid);

	/**
	 * 获取服务最新基线
	 *
	 * @param uuid
	 * @return
	 */
	KnowledgePackageBaseline selectLeastBaseline(String uuid);

	/**
	 * 查询上线的服务uuid集合
	 *
	 * @return
	 */
	List<String> selectOnlineMicro();

	/**
	 * 根据知识包uuid删除服务
	 *
	 * @param pkgUuid
	 * @return
	 */
	int deleteByPackageUuid(String pkgUuid);

	String selectProjectUuidByMicroUuid(@Param("microUuid") String microUuid);

	/**
	 * 导入基线，查询是否存在
	 *
	 * @param micro
	 * @return
	 */
	Micro selectOnUploadBaseline(Micro micro);

	/**
	 * 导入基线更新
	 *
	 * @param micro
	 * @return
	 */
	Micro updateOnUploadBaseline(Micro micro);
}
