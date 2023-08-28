package com.beagledata.gaea.batch.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 描述:
 *
 * @author zgx
 * @date 2020-11-04
 */
@Mapper
public interface ExecuteMapper {
	/**
	* 描述: 插入执行结果到输出表
	* @param: [outputTableName, output, bizDate, seqNo]
	* @author: 周庚新
	* @date: 2020/11/9 
	* @return: int
	*/
	int insertOutput(@Param("outputTableName")String outputTableName, @Param("output")String output,
					 @Param("bizDate")String bizDate, @Param("seqNo")String seqNo);
}
