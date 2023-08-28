package com.beagledata.gaea.executionserver.scheduled;

import com.alibaba.fastjson.JSON;
import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.common.task.AbstractTask;
import com.beagledata.gaea.executioncore.entity.DecisionLog;
import com.beagledata.gaea.executioncore.service.DecisionLogService;
import com.beagledata.gaea.executionserver.common.ResourceResolver;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 描述:
 * 入库失败日志重新入库定时任务
 * @author 周庚新
 * @date 2020-11-10
 */
public class FailDecisionLogWarehousingTask extends AbstractTask {
	private Logger logger = LoggerFactory.getLogger(this.getClass());


	@Override
	public String desc() {
		return "入库失败日志重新入库";
	}

	@Override
	public void run() {
		logger.info("入库失败日志重新入库");
		ResourceResolver resourceResolver = SpringBeanHolder.getBean(ResourceResolver.class);
		DecisionLogService decisionLogService = SpringBeanHolder.getBean(DecisionLogService.class);
		try {
			File failInsert = SafelyFiles.newFile(resourceResolver.getDecisionLogPath() + File.separator + "failInsert");
			File[] failInsertFiles = failInsert.listFiles();
			while (failInsertFiles != null && failInsertFiles.length > 0) {
				for (File file : failInsertFiles) {
					try {
						String data = FileUtils.readFileToString(file);
						DecisionLog decisionLog = JSON.parseObject(data, DecisionLog.class);
						decisionLogService.insert(decisionLog);
						boolean flag = file.delete();
						if (!flag) {
							System.gc();
							file.delete();
						}
					} catch (Exception e) {
					}
				}
				failInsert = SafelyFiles.newFile(resourceResolver.getDecisionLogPath() + File.separator + "failInsert");
				failInsertFiles = failInsert.listFiles();
			}
		} catch (Exception e) {
			logger.error("决策日志日志重新入库失败", e);
		}

		try {
			File failUpdate = SafelyFiles.newFile(resourceResolver.getDecisionLogPath() + File.separator + "failUpdate");
			File[] failUpdateFiles = failUpdate.listFiles();
			while (failUpdateFiles != null && failUpdateFiles.length > 0) {
				for (File file : failUpdateFiles) {
					try {
						String data = FileUtils.readFileToString(file);
						DecisionLog decisionLog = JSON.parseObject(data, DecisionLog.class);
						decisionLogService.update(decisionLog);
						boolean flag = file.delete();
						if (!flag) {
							System.gc();
							file.delete();
						}
					} catch (Exception e) {
					}
				}
				failUpdate = SafelyFiles.newFile(resourceResolver.getDecisionLogPath() + File.separator + "failUpdate");
				failUpdateFiles = failUpdate.listFiles();
			}
		} catch (Exception e) {
			logger.error("决策日志日志重新入库失败", e);
		}
		logger.info("入库失败日志重新入库结束");
	}
}