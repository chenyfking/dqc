package com.beagledata.gaea.executionserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.beagledata.gaea.executioncore.entity.DecisionLog;
import com.beagledata.gaea.executioncore.service.DecisionLogService;
import com.beagledata.gaea.executionserver.common.ResourceResolver;
import com.beagledata.gaea.executionserver.mapper.DecisionLogMapper;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.ruleengine.util.ThreadPoolFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by liulu on 2020/7/21.
 */
@Service
public class DecisionLogServiceImpl implements DecisionLogService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private DecisionLogMapper decisionLogMapper;
	@Autowired
	private ResourceResolver resourceResolver;

	private ExecutorService executorService = ThreadPoolFactory.newThreadPool(3, 10, 1000 * 60, 1000, new ThreadPoolExecutor.CallerRunsPolicy());

    @PostConstruct
    public void init() {
        createTable();
    }

	@PreDestroy
	public void destroy() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
	}

	@Override
	public void add(DecisionLog log) {
        DecisionLog event = new DecisionLog();
        copy(event, log);
        insert(event);
	}

	@Override
	public void insert(DecisionLog log) {
        try {
            decisionLogMapper.insert(log);
        } catch (Exception e) {
            logger.error("决策日志入库失败", e);
            // 启动线程将日志缓存到磁盘
            executorService.submit(() -> flushOnFailInsert(log));
        }
	}

	@Override
	public void update(DecisionLog log) {
        try {
            decisionLogMapper.update(log);
        } catch (Exception e) {
            logger.error("决策日志更新失败", e);
            // 启动线程将日志缓存到磁盘
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    flushOnFailUpdate(log);
                }
            });
        }
	}

	@Override
	public void createTable() {
        Date nowDate = new Date();
        Date nextMonthDate = DateUtils.addMonths(nowDate, 1);
        String month = DateFormatUtils.format(nowDate, "yyyyMM");
        String nextMonth = DateFormatUtils.format(nextMonthDate, "yyyyMM");
        decisionLogMapper.createTable(month);
        decisionLogMapper.createTable(nextMonth);
	}

	@Override
	public void flushOnFailInsert(DecisionLog log) {
        try {
            FileUtils.write(SafelyFiles.newFile(resourceResolver.getDecisionLogPath() + File.separator + "failInsert" + File.separator + log.getUuid()+ ".txt"), JSON.toJSONString(log), "UTF-8");
        } catch (IOException e) {
            logger.error("写出插入决策日志到磁盘出错：{}", JSON.toJSONString(log), e);
        }
	}

	@Override
	public void flushOnFailUpdate(DecisionLog log) {
        try {
            FileUtils.write(SafelyFiles.newFile(resourceResolver.getDecisionLogPath() + File.separator + "failUpdate" + File.separator + log.getUuid()+ ".txt"), JSON.toJSONString(log), "UTF-8");
        } catch (IOException e) {
            logger.error("写出更新决策日志到磁盘出错：{}", JSON.toJSONString(log), e);
        }
	}

	private void copy(DecisionLog event, DecisionLog log) {
		event.setUuid(log.getUuid());
		event.setOrgUuid(log.getOrgUuid());
		event.setOrgName(log.getOrgName());
		event.setUserUuid(log.getUserUuid());
		event.setUserName(log.getUserName());
		event.setMicroUuid(log.getMicroUuid());
		event.setDeployUuid(log.getDeployUuid());
		event.setPkgUuid(log.getPkgUuid());
		event.setPkgBaseline(log.getPkgBaseline());
		event.setReqType(DecisionLog.REQ_TYPE_ONLINE);
		event.setState(log.getState());
		event.setReqText(log.getReqText());
		event.setResText(log.getResText());
		event.setFullText(log.getFullText());
		event.setLogTableMonth(log.getLogTableMonth());
		event.setCreateTime(log.getCreateTime());
		event.setPass(log.getPass());
		event.setEffective(log.getEffective());
		event.setDecisionTrace(log.getDecisionTrace());
	}
}
