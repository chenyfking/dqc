package com.beagledata.gaea.batch.queue;

import com.beagledata.gaea.batch.configs.Configs;
import com.beagledata.gaea.batch.mapper.ExecuteMapper;
import com.beagledata.gaea.executioncore.MicroExecutor;
import com.beagledata.gaea.executioncore.domain.Input;
import com.beagledata.gaea.executioncore.domain.Output;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-11-04
 */
@Service
public class DefaultEventHandler implements EventHandler {
	@Autowired
	private Configs configs;
	@Autowired
	MicroExecutor microExecutor;
	@Autowired
	ExecuteMapper executeMapper;
	@Override
	public void onEvent(Map<String, Object> input) throws Exception {
		String service = configs.getMicroUuid();
		Input inputVo = new Input();
		inputVo.setService(service);
		inputVo.setReqDate(new Date());
		inputVo.setUuid(IdUtils.UUID());
		inputVo.setAsync(false);
		inputVo.setData(input);

		Output output = microExecutor.execute(inputVo);
		Object res = output.getResponse();
		String resText = StringUtils.EMPTY;
		if (res != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			resText = objectMapper.writeValueAsString(res);
		}
		executeMapper.insertOutput(configs.getOutputTableName(), resText, configs.getBizDate(), output.getSeqNo());
	}
}