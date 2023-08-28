package com.beagledata.gaea.batch.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author zgx
 * @date 2020-11-04
 */
@Mapper
public interface DispatcherMapper {

	/**
	 * 描述: 标记可处理的记录
	 * @param: [tableName, processingOwner, processingTime, timeout, limit]
	 * @author: 周庚新
	 * @date: 2020/11/4
	 * @return: int
	 */
	int updateClaimEntries(@Param("tableName") String tableName, @Param("processingOwner") String processingOwner,
						   @Param("processingTime") String processingTime, @Param("timeout") String timeout, @Param("limit") int limit);

	/**
	* 描述: 查询可处理的记录
	* @param: [tableName, processingOwner, processingTime, limit]
	* @author: 周庚新
	* @date: 2020/11/4 
	* @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
	*/
	List<Map<String, Object>> selectReadyEntries(@Param("tableName") String tableName, @Param("processingOwner") String processingOwner,
												 @Param("processingTime") String processingTime, @Param("limit") int limit);
	/**
	* 描述: 记录状态变更为PROCESSED
	* @param: [tableName, id]
	* @author: 周庚新
	* @date: 2020/11/4 
	* @return: int
	*/
	int updateOnSuccess(@Param("tableName") String tableName, @Param("id")long id);
	/**
	* 描述: 记录状态变更为AVAILABE 失败次数+1，去掉处理标记
	* @param: [tableName, id]
	* @author: 周庚新
	* @date: 2020/11/4 
	* @return: int
	*/
	int updateOnError(@Param("tableName") String tableName, @Param("id")long id);
	/**
	* 描述: 记录状态变更为FAILED
	* @param: [tableName, id]
	* @author: 周庚新
	* @date: 2020/11/4
	* @return: int
	*/
	int updateOnFailed(@Param("tableName") String tableName, @Param("id")long id);
	/**
	* 描述: 查询表字段清单
	* @param: [tableName]
	* @author: 周庚新
	* @date: 2020/11/4
	* @return: java.util.List<java.lang.String>
	*/
	List<String> selectInputTableColumns(@Param("tableName") String tableName);
	/**
	* 描述: 判断输入表是否存在
	* @param: [tableName]
	* @author: 周庚新
	* @date: 2020/11/9 
	* @return: int
	*/
	int countInputTableForExists(@Param("tableName") String tableName);
	/**
	* 描述: 添加表字段
	* @param: [tableName]
	* @author: 周庚新
	* @date: 2020/11/4
	* @return: int
	*/
	int updateInputTableColumns(@Param("tableName") String tableName);
	/**
	* 描述: 获取输入表记录总数
	* @param: [tableName, timeout]
	* @author: 周庚新
	* @date: 2020/11/4
	* @return: int
	*/
	int countInputTableEntries(@Param("tableName") String tableName, @Param("timeout")String timeout);
	/**
	* 描述: 创建输出表
	* @param: [tableName]
	* @author: 周庚新
	* @date: 2020/11/4
	* @return: int
	*/
	int createOutputTable(@Param("tableName") String tableName);
}
