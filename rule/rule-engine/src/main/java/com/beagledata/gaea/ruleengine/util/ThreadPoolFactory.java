package com.beagledata.gaea.ruleengine.util;

import java.util.concurrent.*;

/**
 * 描述:
 * 线程池工厂类
 *
 * @author 周庚新
 * @date 2020-09-16
 */
public class ThreadPoolFactory {

	/**
	* 描述: 创建线程池
	* @param: [corePoolSize, maximumPoolSize, keepAliveTime, queueSize, rejectHandler]  最小线程数，最大线程数，线程空闲时间；单毫秒，提交任务队列容量大小，提交失败处理逻辑
	* @author: 周庚新
	* @date: 2020/9/16
	* @return: java.util.concurrent.ExecutorService 线程池对相关
	*
	*/
	public static ExecutorService newThreadPool(int corePoolSize, int maximumPoolSize , long keepAliveTime, int queueSize, RejectedExecutionHandler rejectHandler) {
		return new ThreadPoolExecutor(corePoolSize,
				maximumPoolSize,
				keepAliveTime,
				TimeUnit.MILLISECONDS,
				new LinkedBlockingDeque<>(queueSize),
				rejectHandler);
	}
}