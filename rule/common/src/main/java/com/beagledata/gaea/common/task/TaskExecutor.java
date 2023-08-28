package com.beagledata.gaea.common.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述:
 * 任务执行器
 *
 * @author 周庚新
 * @date 2020-10-14
 */
public class TaskExecutor implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger(TaskExecutor.class);

	private Task task;
	private TaskLocker taskLocker;

	public TaskExecutor(Task task, TaskLocker taskLocker) {
		this.task = task;
		this.taskLocker = taskLocker;
	}

	@Override
	public void run() {
		if (taskLocker.lock()) {
		    logger.info("开始执行任务. desc: {}, cron: {}, class: {}", task.desc(), task.cron(), task.getClass().getName());
		    try {
		    	task.run();
			} finally {
		    	taskLocker.unlock();
				logger.info("结束执行任务. desc: {}, cron: {}, class: {}", task.desc(), task.cron(), task.getClass().getName());
			}
		}
	}
}