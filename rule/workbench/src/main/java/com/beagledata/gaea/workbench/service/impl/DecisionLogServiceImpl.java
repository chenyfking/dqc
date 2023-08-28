package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.DecisionLog;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.mapper.DecisionLogMapper;
import com.beagledata.gaea.workbench.mapper.MicroMapper;
import com.beagledata.gaea.workbench.service.DecisionLogService;
import com.beagledata.util.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liulu on 2020/11/12.
 */
@Service
public class DecisionLogServiceImpl implements DecisionLogService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DecisionLogMapper decisionLogMapper;
    @Autowired
    private AssetsMapper assetsMapper;
    @Autowired
    private MicroMapper microMapper;

    @Override
    public void downloadDecisionLog(String microUuid, Date startDate, Date endDate, HttpServletResponse response) {
        StringBuilder buffer = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        while (startDate.before(endDate)) {
            Date monthEnd = getMonthEnd(startDate);
            if (endDate.before(monthEnd)) {
                monthEnd = endDate;
            }

            int start = 0;
            int end = 1000;
            int size;
            String tableMonth = sdf.format(startDate);
            do {
                List<DecisionLog> decisionLogs = decisionLogMapper.selectPages(start, end, microUuid, startDate, monthEnd, tableMonth);
                size = decisionLogs.size();
                if (size > 0) {
                    decisionLogs.forEach(decisionLog -> buffer.append(JSON.toJSONString(decisionLog)).append("\n"));
                }
                size += 1000;
            } while (size == 1000);
            startDate = getMonthStart(DateUtils.addMonths(startDate, 1));
        }

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=logs.txt");
        try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            bos.write(buffer.toString().getBytes("UTF-8"));
            bos.flush();
        } catch (IOException e) {
            logger.error("导出决策日志失败. microUuid: {}, startDate: {}, endDate: {}", microUuid, startDate, endDate, e);
            throw new IllegalStateException("导出决策日志失败");
        }
    }

    @Override
    public Result trace(String seqNo) {
        if (StringUtils.isBlank(seqNo) || seqNo.length() != 38) {
            throw new IllegalArgumentException("流水号不正确");
        }

        String tableMonth = seqNo.substring(0, 6);
        String uuid = seqNo.substring(6);
        DecisionLog decisionLog = new DecisionLog();
        decisionLog.setUuid(uuid);
        decisionLog.setLogTableMonth(tableMonth);
        Integer state;
        String projectUuid;
        try {
            DecisionLog dl = decisionLogMapper.selectStateAndMicroUuid(decisionLog);
            if (dl == null) {
                throw new IllegalArgumentException("流水号不正确");
            }
            state = dl.getState();
            projectUuid = microMapper.selectProjectUuidByMicroUuid(dl.getMicroUuid());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("流水号查询轨迹失败. seqNo: {}", seqNo, e);
            throw new IllegalStateException("查询失败");
        }

        if (state == null) {
            throw new IllegalStateException("没有查询到结果");
        }
        if (state == DecisionLog.STATE_RUNNING) {
            return Result.newInstance(DecisionLog.STATE_RUNNING, "决策正在执行");
        }
        if (state == DecisionLog.STATE_FAIL) {
            return Result.newInstance(DecisionLog.STATE_FAIL, "决策执行失败");
        }

        try {
            decisionLog = decisionLogMapper.selectTrace(decisionLog);
            if (decisionLog == null) {
                return Result.newError().withMsg("没有查询到结果");
            }

            JSONObject executionResult = JSON.parseObject(decisionLog.getDecisionTrace());
            JSONObject fireNodeMap = executionResult.getJSONObject("fireNodes");
            JSONArray fireNodes = new JSONArray();
            if (!fireNodeMap.isEmpty()) {
                for (Map.Entry<String, Object> entry : fireNodeMap.entrySet()) {
                    String[] keys = entry.getKey().split("_");
                    String assetsUuid = keys[1];
                    String versionNo = keys[2];
                    Assets flow = assetsMapper.selectByUuid(assetsUuid);
                    if (flow == null) {
                        continue;
                    }

                    String flowName = flow.getName();
                    if (!"0".equals(versionNo)) {
                        flowName = flowName + "_V" + versionNo;
                    }
                    JSONObject flowNode = new JSONObject();
                    flowNode.put("flowUuid", assetsUuid);
                    flowNode.put("flowName", flowName);
                    flowNode.put("processId", entry.getKey());
                    flowNode.put("versionNo", NumberUtils.toInt(versionNo));
                    if (entry.getValue() != null) {
                        flowNode.put("nodes", entry.getValue());
                    }
                    fireNodes.add(flowNode);
                }
            }

            executionResult.put("fireNodes", fireNodes);
            Map<String, Object> res = new HashMap<>(3);
            res.put("trace", executionResult);
            res.put("projectUuid", projectUuid);
            res.put("result", decisionLog.getFullText());
            return Result.newSuccess().withData(res);
        } catch (Exception e) {
            logger.error("流水号查询轨迹失败. seqNo: {}", seqNo, e);
            throw new IllegalStateException("查询失败");
        }
    }

    /**
     * 获取日期所属月份的结束时间
     *
     * @param date
     * @return
     */
    private Date getMonthEnd(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        LocalDate lastDay = localDate.with(TemporalAdjusters.lastDayOfMonth());
        String lastDayStr = lastDay.toString() + " 23:59:59";
        try {
            return DateUtils.parseDate(lastDayStr, "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            return date;
        }
    }

    private Date getMonthStart(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        LocalDate firstDay = localDate.with(TemporalAdjusters.firstDayOfMonth());
        String startDayStr = firstDay.toString() + " 00:00:00";
        try {
            return DateUtils.parseDate(startDayStr, "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            return date;
        }
    }
}
