package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务关联的文件，AI模型
 * @author chenyafeng
 * @date 2018/12/19
 */
@Mapper
public interface MicroRelationMapper {
	/**
	 * 批量新增或修改与模型的关联
	 */
	void batchInsertOrUpdateModel(@Param("list") List<MicroModel> list);

	/**
	 * 删除服务使用的模型
	 * @param microUuid 服务的uuid
	 */
	void deleteModels(@Param("microUuid")String microUuid);

	/**
	 * 根据知识包uuid查询服务(Project)名称和知识包名称，sql中服务名称字段存放在知识包的description字段
	 * Created by Chenyafeng on 2019/11/27
	 */
	KnowledgePackage selectPrjAndPackageName(String packageUuid);

	/**
	 * 批量保存服务和集群节点的关联
	 *
	 * @param micro
	 * @return
	 */
	int insertBatchMicroClientRoute(Micro micro);

	/**
	 * 保存服务和集群节点的关联
	 *
	 * @param clientUuid
	 * @param microUuid
	 * @return
	 */
	int insertOrUpdateMicroClientRoute(@Param("clientUuid") String clientUuid, @Param("microUuid") String microUuid);

	/**
     * @Author yangyongqiang
     * @Description 下线决策服务
     * @Date 11:19 上午 2020/7/3
     **/
    void deleteByMicroUuid(@Param("microUuid")String microUuid);

    void deleteByMicroAndClient(@Param("microUuid") String microUuid, @Param("clientUuid") String clientUuid);
}
