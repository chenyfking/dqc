package com.beagledata.gaea.batch.service.impl;

import com.alibaba.fastjson.JSON;
import com.beagledata.gaea.batch.configs.Configs;
import com.beagledata.gaea.batch.mapper.DecisionLogMpper;
import com.beagledata.gaea.executioncore.entity.DecisionLog;
import com.beagledata.gaea.executioncore.service.DecisionLogService;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @Auther: yinrj
 * @Date: 0023 2020/7/23 20:13
 * @Description:
 */
@Service
public class DecisionLogServiceImpl implements DecisionLogService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DecisionLogMpper decisionLogMapper;
    @Autowired
    private Configs configs;

    @PostConstruct
    private void initLogTable() {
        // 启动时创建本月和下月的决策日志表
        logger.info("初始化决策日志表");
        createTable();
    }

    @Override
    public void add(DecisionLog log) {
        try {
            log.setReqType(DecisionLog.REQ_TYPE_BATCH);
            decisionLogMapper.insert(log);
        } catch (Exception e){
            logger.error("跑批任务决策日志入库失败", e);
            flushOnFailInsert(log);
        }
    }

    @Override
    public void insert(DecisionLog log) {

    }

    @Override
    public void update(DecisionLog log) {

    }

    @Override
    public void createTable() {
        Date nowDate = new Date();
        Date nextMonthDate = DateUtils.addMonths(nowDate, 1);
        Date lastMonthDate = DateUtils.addMonths(nowDate, 1);
        String month = DateFormatUtils.format(nowDate, "yyyyMM");
        String nextMonth = DateFormatUtils.format(nextMonthDate, "yyyyMM");
        String lastMonth = DateFormatUtils.format(lastMonthDate, "yyyyMM");
        decisionLogMapper.createTable(lastMonth);
        decisionLogMapper.createTable(month);
        decisionLogMapper.createTable(nextMonth);
    }

    @Override
    public void flushOnFailInsert(DecisionLog log) {
        try {
            FileUtils.write(SafelyFiles.newFile(configs.getDecisionLogPath() + File.separator + "failInsert" + File.separator + log.getUuid()+ ".txt"), JSON.toJSONString(log), "UTF-8");
        } catch (IOException e) {
            logger.error("写出插入决策日志到磁盘出错：{}", JSON.toJSONString(log), e);
        }
    }

    @Override
    public void flushOnFailUpdate(DecisionLog log) {
        try {
            FileUtils.write(SafelyFiles.newFile(configs.getDecisionLogPath() + File.separator + "failUpdate" + File.separator + log.getUuid()+ ".txt"), JSON.toJSONString(log), "UTF-8");
        } catch (IOException e) {
            logger.error("写出更新决策日志到磁盘出错：{}", JSON.toJSONString(log), e);
        }
    }
}
