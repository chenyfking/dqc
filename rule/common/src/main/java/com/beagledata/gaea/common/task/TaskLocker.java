package com.beagledata.gaea.common.task;

/**
 * 描述:
 * 任务锁
 *
 * @author 周庚新
 * @date 2020-09-18
 */
public interface TaskLocker {
	
	/**
	* 描述: 获取锁
	* @param: []
	* @author: 周庚新
	* @date: 2020/9/18 
	* @return: boolean
	* 
	*/
	boolean lock();
	
	/**
	* 描述: 释放锁
	* @param: []
	* @author: 周庚新
	* @date: 2020/9/18 
	* @return: void
	* 
	*/
	void unlock();
	
}