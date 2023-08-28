package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.FunctionDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by liulu on 2020/6/10.
 */
@Mapper
public interface FunctionDefinitionMapper {
	/**
	 * @param function
	 * @return
	 */
	int insert(FunctionDefinition function);

	/**
	 * @return
	 */
	List<FunctionDefinition> selectAll();

	/**
	 * @param name
	 * @return
	 */
	FunctionDefinition selectByName(String name);

	/**
	 * @param className
	 * @return
	 */
	FunctionDefinition selectByClassName(String className);

	/**
	 * @param uuid
	 * @return
	 */
	int delete(String uuid);

	/**
	 * @author liulu
	 * 2020/7/1 10:14
	 */
	FunctionDefinition selectByUuid(String uuid);

	/**
	 * 描述: 更新
	 * @param: [functionDefinition]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: int
	 */
	int update(FunctionDefinition functionDefinition);

	/**
	 * 描述: 根据名称或者类名称查询
	 * @param: [name, className]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.FunctionDefinition>
	 */
	List<FunctionDefinition> selectByNameOrClassName(@Param("name") String name, @Param("className") String className);

	/**
	 * 描述: 导入基线查询是否存在
	 * @param: [aiModel]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: com.beagledata.gaea.workbench.entity.AiModel
	 */
	FunctionDefinition selectOnUploadBaseline(FunctionDefinition functionDefinition);

	/**
	 * 描述: 导入基线，更新
	 * @param: [aiModel]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: void
	 */
	void updateOnUploadBaseline(FunctionDefinition functionDefinition);
}
