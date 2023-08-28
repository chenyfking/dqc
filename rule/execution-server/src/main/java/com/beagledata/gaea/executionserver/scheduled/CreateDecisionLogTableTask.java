package com.beagledata.gaea.executionserver.scheduled;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.common.task.AbstractTask;
import com.beagledata.gaea.executioncore.service.DecisionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述:
 * 创建决策日志表定时任务
 *
 * @author 周庚新
 * @date 2020-11-10
 */
public class CreateDecisionLogTableTask extends AbstractTask {
	private Logger logger = LoggerFactory.getLogger(this.getClass());


	@Override
	public String desc() {
		return "初始化决策日志表";
	}

	@Override
	public void run() {
		logger.info("初始化决策日志表");
		try {
			DecisionLogService decisionLogService = SpringBeanHolder.getBean(DecisionLogService.class);
			decisionLogService.createTable();
			logger.info("初始化决策日志表结束");
		} catch (Exception e) {
			logger.error("初始化决策日志表失败", e);
		}
	}
}