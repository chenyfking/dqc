package com.beagledata.gaea.batch.mapper;

import com.beagledata.gaea.executioncore.entity.DecisionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 描述:
 * 决策日志mapper
 *
 * @author 周庚新
 * @date 2020-11-04
 */
@Mapper
public interface DecisionLogMpper {
	/**
	* 描述: 插入决策日志
	* @param: [decisionLog]
	* @author: 周庚新
	* @date: 2020/11/4 
	* @return: int
	* 
	*/
	int insert(DecisionLog decisionLog);
	
	/**
	* 描述: 添加决策服务文本
	* @param: [decisionLog]
	* @author: 周庚新
	* @date: 2020/11/4 
	* @return: int
	* 
	*/
	int insertText(DecisionLog decisionLog);
	
	/**
	* 描述: 批量插入决策日志
	* @param: [decisionLogs]
	* @author: 周庚新
	* @date: 2020/11/4 
	* @return: int
	* 
	*/
	int insertBatch(List<DecisionLog> decisionLogs);
	
	/**
	* 描述: 根据月份初始化决策日志表
	* @param: [logTableMonth]
	* @author: 周庚新
	* @date: 2020/11/4 
	* @return: void
	* 
	*/
	void createTable(@Param("logTableMonth")String logTableMonth);
}