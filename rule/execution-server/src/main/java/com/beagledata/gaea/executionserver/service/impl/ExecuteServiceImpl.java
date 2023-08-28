package com.beagledata.gaea.executionserver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.beagledata.common.Result;
import com.beagledata.gaea.executioncore.MicroExecutor;
import com.beagledata.gaea.executioncore.domain.Input;
import com.beagledata.gaea.executioncore.domain.Output;
import com.beagledata.gaea.executioncore.entity.DecisionLog;
import com.beagledata.gaea.executionserver.common.ResourceResolver;
import com.beagledata.gaea.executionserver.mapper.DecisionLogMapper;
import com.beagledata.gaea.executionserver.service.ExecuteService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

/**
 * Created by Chenyafeng on 2018/12/28.
 */
@Service
public class ExecuteServiceImpl implements ExecuteService {
	private static Logger logger = LoggerFactory.getLogger(ExecuteServiceImpl.class);

	@Autowired
	private ResourceResolver resourceResolver;
	@Autowired
	private MicroExecutor microExecutor;
	@Autowired
	private DecisionLogMapper decisionLogMapper;

	@Override
	public Result uploadLicense(InputStream inputStream) {
		try {
			String license = IOUtils.toString(inputStream);
			FileUtils.writeStringToFile(new File(resourceResolver.getLicensePath()), license, "UTF-8");
			return Result.newSuccess();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new IllegalStateException("上传license失败");
		}
	}

	@Override
	public Output execute(Input input) {
		return microExecutor.execute(input);
	}

	@Override
	public Output executeAsync(Input input) {
		return microExecutor.executeAsync(input);
	}

	@Override
	public Result query(String seqNo) {
		if (StringUtils.isBlank(seqNo) || seqNo.length() != 38) {
			throw new IllegalArgumentException("流水号不正确");
		}

		String tableMonth = seqNo.substring(0, 6);
		String uuid = seqNo.substring(6);
		DecisionLog decisionLog = new DecisionLog();
		decisionLog.setUuid(uuid);
		decisionLog.setLogTableMonth(tableMonth);

		Integer state = null;
		try {
			state = decisionLogMapper.selectState(decisionLog);
		} catch (Exception e) {
			logger.error("流水号查询结果失败. seqNo: {}", seqNo, e);
			throw new IllegalStateException("查询失败");
		}

		if (state == null) {
			throw new IllegalStateException("没有查询到结果");
		}

		if (state == DecisionLog.STATE_RUNNING) {
			return Result.newSuccess().withCode(DecisionLog.STATE_RUNNING).withMsg("正在执行...");
		}

		if (state == DecisionLog.STATE_FAIL) {
			return Result.newSuccess().withCode(DecisionLog.STATE_FAIL).withMsg("执行失败，请重新提交");
		}

		String data = null;
		try {
			data = decisionLogMapper.selectUnSyncResult(decisionLog);
		} catch (Exception e) {
			logger.error("流水号查询结果失败. seqNo: {}", seqNo, e);
			throw new IllegalStateException("查询失败");
		}

		Output output = new Output(seqNo);
		if (StringUtils.isNotBlank(data)) {
			output.setResponse(JSONObject.parseObject(data));
		} else {
			output.setResponse(Collections.EMPTY_MAP);
		}
		return Result.newSuccess().withData(output);
	}
}
