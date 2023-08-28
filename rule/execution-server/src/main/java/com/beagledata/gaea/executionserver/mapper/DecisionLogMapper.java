package com.beagledata.gaea.executionserver.mapper;

import com.beagledata.gaea.executioncore.entity.DecisionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author yangyongqiang
 * @Description 决策服务日志
 * @Date 2:20 下午 2020/7/21
 **/
@Mapper
public interface DecisionLogMapper {
	/**
	 * @Author yangyongqiang
	 * @Description 添加决策服务日志
	 * @Date 2:34 下午 2020/7/21
	 **/
	int insert(DecisionLog decisionLog);

	/**
	 * @Author yangyongqiang
	 * @Description 批量添加决策服务日志
	 * @Date 6:47 下午 2020/7/21
	 **/
	int insertBatch(List<DecisionLog> decisionLog);

	/**
	 * 描述: 更新决策日志
	 * @param: [decisionLog]
	 * @author: 周庚新
	 * @date: 2020/11/9
	 * @return: void
	 */
	void update(DecisionLog decisionLog);

	/**
	 * 描述: 查询异步执行结果
	 * @param: [decisionLog]
	 * @author: 周庚新
	 * @date: 2020/11/9
	 * @return: java.lang.String
	 */
	String selectUnSyncResult(DecisionLog decisionLog);

	/**
	 * 描述: 查询执行状态
	 * @param: [decisionLog]
	 * @author: 周庚新
	 * @date: 2020/11/9
	 * @return: java.lang.Integer
	 */
	Integer selectState(DecisionLog decisionLog);

	/**
	 * 描述: 根据月份初始化决策日志表
	 * @param: [logTableMonth]
	 * @author: 周庚新
	 * @date: 2020/11/9
	 * @return: int
	 */
	int createTable(@Param("logTableMonth") String logTableMonth);
}
