package com.beagledata.gaea.batch.service.impl;

import com.beagledata.gaea.batch.service.LogService;
import com.beagledata.gaea.executioncore.entity.DecisionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-11-04
 */
public class LogServiceImpl implements LogService {

	private List<DecisionLog> successLog = new ArrayList<>();

	@Override
	public void add(DecisionLog decisionLog) {
		decisionLog.setReqType(DecisionLog.REQ_TYPE_BATCH);
		successLog.add(decisionLog);
	}

	@Override
	public List<DecisionLog> getSuccessLog() {
		return successLog;
	}
}