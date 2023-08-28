package com.beagledata.gaea.common.task;

/**
 * 描述:
 * 任务定义
 *
 * @author zgx
 * @date 2020-09-18
 */
public interface Task extends Runnable {
	/**
	* 描述: 任务描述
	* @param: []
	* @author: 周庚新
	* @date: 2020/9/18 
	* @return: java.lang.String
	* 
	*/
	String desc();
	
	/**
	* 描述: cron 配置
	* @param: []
	* @author: 周庚新
	* @date: 2020/9/18 
	* @return: java.lang.String
	* 
	*/
	String cron();
}
