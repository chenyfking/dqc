package com.beagledata.gaea.ruleengine.function.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 描述:
 * jdbc 代理
 *
 * @author zgx
 * @date 2020-09-15
 */
public interface JdbcTemplateProxy {

	/**
	*
	* @param: [sql]
	* @author: zgx
	* @date: 2020/9/15
	* @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
	*
	*/
	List<Map<String, Object>> queryForList(String sql);

	/**
	* 描述:
	* @param: [sql, org]
	* @author: 周庚新
	* @date: 2020/9/15
	* @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
	*
	*/
	List<Map<String, Object>> queryForList(String sql, Object... args);

	/**
	* 描述: 统计总数
	* @param: [sql]
	* @author: 周庚新
	* @date: 2020/12/16
	* @return: int
	*/
	int queryCount(String sql);

	int queryCount(String sql, Object... args);
}