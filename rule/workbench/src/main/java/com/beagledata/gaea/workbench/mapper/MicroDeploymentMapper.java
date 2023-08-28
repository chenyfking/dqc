package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.MicroDeployment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务
 * @author chenyafeng
 * @date 2018/12/3
 */
@Mapper
public interface MicroDeploymentMapper {
	/**
	 * @Author yangyongqiang
	 * @Description 新增或修改
	 * @Date 4:25 下午 2020/7/14
	 **/
	void insertOrUpdate(MicroDeployment microDeployment);

	/**
	 * @Author yangyongqiang
	 * @Description 新增模型
	 * @Date 7:09 下午 2020/7/14
	 **/
	void insertModel(@Param("microDeployment") MicroDeployment microDeployment);

	/**
	 * @Author yangyongqiang
	 * @Description 查询优化模型
	 * @Date 2:14 下午 2020/7/15
	 **/
	List<MicroDeployment> selectByMicroUuid(
			@Param("microUuid")String microUuid,
			@Param("start") Integer start,
			@Param("limit") Integer limit
	);

	/**
	 * @Author yinrj
	 * @Description 查询所有生效服务
	 * @Date 2021/1/11
	 **/
	List<MicroDeployment> selectAll();

	/**
	 * @Author yangyongqiang
	 * @Description 模型总数
	 * @Date 3:21 下午 2020/7/24
	 **/
	int countByMicroUuid(@Param("microUuid")String microUuid);
}
