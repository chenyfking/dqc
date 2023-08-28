package com.beagledata.gaea.batch.service;

import com.beagledata.gaea.executioncore.entity.DecisionLog;

import java.util.List;

/**
 * 描述:
 * 决策日志
 *
 * @author 周庚新
 * @date 2020-11-04
 */
public interface LogService {

	void add(DecisionLog decisionLog);

	List<DecisionLog> getSuccessLog();
}