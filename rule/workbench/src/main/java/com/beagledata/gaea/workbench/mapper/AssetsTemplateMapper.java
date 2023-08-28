package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.AssetsTemplate;
import com.beagledata.gaea.workbench.vo.AssetsTreeNodeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 规则模板
 * Created by Chenyafeng on 2020/6/12
 */
@Mapper
public interface AssetsTemplateMapper {

	void insert(AssetsTemplate assetsTemplate);

	/**
	 * 根据uuid更新模板内容
	 * @param uuid
	 * @param content
	 */
	void updateContentByUuid(@Param("uuid") String uuid,
							 @Param("content") String content);

	void delete(String uuid);

	AssetsTemplate selectByUuid(String uuid);

	List<AssetsTemplate> selectByParams(@Param("assetsTemplate") AssetsTemplate assetsTemplate);

	List<AssetsTreeNodeVO> getAssetsTreeNode(@Param("projectUuid") String projectUuid, @Param("type") String type);

	/**
	 * @Author yangyongqiang
	 * @Description 查询资源文件模版相关内容ByProjectUuid
	 * @Date 5:11 下午 2020/7/23
	 **/
	List<AssetsTemplate> selectByProjectUuid(@Param("projectUuid") String projectUuid);

	/**
	 * 描述: 批量插入模板
	 * @param: [assetsTemplates]
	 * @author: 周庚新
	 * @date: 2020/7/24
	 * @return: void
	 */
	void insertBatch(List<AssetsTemplate> assetsTemplates);

	/**
	 * 描述: 根据uuid 查询模板内容
	 * @param: [uuids]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.util.List<java.lang.String>
	 */
	List<String> selectContentByUuidS(Collection<String> uuids);

}
