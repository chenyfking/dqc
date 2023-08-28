package com.beagledata.gaea.executioncore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.beagledata.gaea.common.RestConstants;
import com.beagledata.gaea.executioncore.domain.*;
import com.beagledata.gaea.executioncore.entity.DecisionLog;
import com.beagledata.gaea.executioncore.handler.*;
import com.beagledata.gaea.executioncore.service.DecisionLogService;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.runtime.ExecutionResult;
import com.beagledata.gaea.ruleengine.runtime.RuleEngine;
import com.beagledata.gaea.ruleengine.util.PackageUtils;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.ruleengine.util.ThreadPoolFactory;
import com.beagledata.gaea.ruleengine.util.ZipUtils;
import com.beagledata.util.EncodeUtils;
import com.beagledata.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by liulu on 2020/7/23.
 */
@Service
public class MicroExecutor {
	private Logger logger = LoggerFactory.getLogger(MicroExecutor.class);

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private DecisionLogService decisionLogService;

	private ExecutorService executorService = ThreadPoolFactory.newThreadPool(5, 100,
			1000 * 60, 1000, new ThreadPoolExecutor.CallerRunsPolicy());

	@PreDestroy
	public void destroy() {
		if (!executorService.isShutdown()) {
			executorService.shutdown();
		}
	}

	/**
	 * 加载服务包
	 *
	 * @param is
	 */
	public void load(InputStream is) throws IOException {
		byte[] bytes = readFile(is);
		Map<String, byte[]> resources = ZipUtils.decompression(new ByteArrayInputStream(bytes));
		Micro micro = readConf(resources);
		logger.info("开始加载服务：{}", micro);
		readModel(micro, resources);
		register(micro);
		logger.info("服务加载成功：{}", micro);
	}

	public void unload(String microUuid) {
		Micro micro = MicroFactory.get(microUuid);
		if (micro == null) {
			logger.warn("卸载服务失败, 服务找不到: {}", microUuid);
			return;
		}

		for (MicroDeployment.Model model : micro.getDeployment().getModels()) {
			unloadModel(model);
		}
		MicroFactory.remove(microUuid);
	}

	/**
	 * 描述: 异步执行
	 *
	 * @param: [input]
	 * @author: 周庚新
	 * @date: 2020/11/2
	 * @return: com.beagledata.gaea.executioncore.domain.Output
	 */
	public Output executeAsync(Input input) {
		Micro micro = MicroFactory.get(input.getService());
		if (micro == null) {
			logger.warn("服务找不到: {}", input);
			throw new IllegalArgumentException("服务找不到，请检查参数");
		}
		DecisionLog decisionLog = initDecisionLog(micro, input);
		decisionLog.setReqType(DecisionLog.REQ_TYPE_ONLINE);
		decisionLog.setEffective(true);
		decisionLogService.insert(decisionLog);
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					execute(input, micro);
				} catch (Exception e) {
					logger.error("执行服务失败. input: {}, micro: {}", input, micro, e);
				}
			}
		});
		return new Output(decisionLog.getSeqNo());
	}

	public Output execute(Input input) {
		Micro micro = MicroFactory.get(input.getService());
		if (micro == null) {
			logger.warn("服务找不到: {}", input);
			throw new IllegalArgumentException("服务找不到，请检查参数");
		}
		return execute(input, micro);
	}

	/**
	 * 描述: 初始化决策日志
	 *
	 * @param: [micro, input]
	 * @author: 周庚新
	 * @date: 2020/11/2
	 * @return: com.beagledata.gaea.executioncore.entity.DecisionLog
	 */
	private DecisionLog initDecisionLog(Micro micro, Input input) {
		DecisionLog decisionLog = new DecisionLog();
		Date date = input.getReqDate();
		String logTableMonth = DateFormatUtils.format(date, "yyyyMM");
		decisionLog.setUuid(input.getUuid());
		decisionLog.setCreateTime(date);
		decisionLog.setState(DecisionLog.STATE_RUNNING);
		decisionLog.setMicroUuid(micro.getId());
		decisionLog.setDeployUuid(micro.getDeployment().getId());
		decisionLog.setReqText(JSON.toJSONString(input.getData()));
		decisionLog.setLogTableMonth(logTableMonth);

		MicroOwner microOwner = micro.getOwner();
		if (microOwner != null) {
			decisionLog.setOrgUuid(microOwner.getOrgUuid());
			decisionLog.setOrgName(microOwner.getOrgName());
			decisionLog.setUserUuid(microOwner.getUserUuid());
			decisionLog.setUserName(microOwner.getUserName());
		}
		return decisionLog;
	}

	/**
	 * 描述: 执行服务
	 *
	 * @param: [input, micro]
	 * @author: 周庚新
	 * @date: 2020/11/2
	 * @return: com.beagledata.gaea.executioncore.domain.Output
	 */
	public Output execute(Input input, Micro micro) {
		if (MicroDeployment.DeployType.COMMON.equals(micro.getDeployment().getType())) {
			if (logger.isDebugEnabled()) {
				logger.debug("没有优化，直接执行模型: micro: {}, input: {}", micro, input);
			}
			return doCommon(micro, input);
		}

		Date expiredTime = getExpiredTime(micro);
		if (expiredTime != null && System.currentTimeMillis() > expiredTime.getTime()) {
			// 判断是否到期
			if (logger.isDebugEnabled()) {
				logger.debug("优化到期，直接执行模型: micro: {}, input: {}", micro);
			}
			removeRedisConfs(micro);
			return doCommon(micro, input);
		}

		String incomingQuantityKey = getIncomingQuantityKey(micro);
		if (micro.getDeployment().getIncomingQuantity() > 0) {
			// 扣一次进件量
			long restIncomingQuantity = stringRedisTemplate.boundValueOps(incomingQuantityKey).increment(-1);
			if (restIncomingQuantity < 0) {
				// 进件量不够了
				if (logger.isDebugEnabled()) {
					logger.debug("达到最大进件量，直接执行模型: micro: {}, input: {}", micro);
				}
				removeRedisConfs(micro);
				return doCommon(micro, input);
			}
		}

		try {
			if (MicroDeployment.DeployType.AB_TEST.equals(micro.getDeployment().getType())) {
				return doABTest(micro, input);
			}

			if (MicroDeployment.DeployType.CHAMPION_CHALLENGER.equals(micro.getDeployment().getType())) {
				return doChampionChallenger(micro, input);
			}
		} catch (Exception e) {
			if (micro.getDeployment().getIncomingQuantity() > 0) {
				// 执行失败后，回退进件量
				stringRedisTemplate.boundValueOps(incomingQuantityKey).increment(1);
			}

			logger.error("服务执行失败, micro: {}, input: {}", micro, input, e);
			throw new RuleException("服务执行失败，请稍候再试");
		}
		throw new IllegalArgumentException("服务执行失败，请检查参数");
	}

	/**
	 * 读取文件流，base64解码
	 *
	 * @param is
	 * @return
	 */
	private byte[] readFile(InputStream is) {
		try {
			byte[] bytes = IOUtils.toByteArray(is);
			bytes = PackageUtils.decrypt(bytes);
			return bytes;
		} catch (Exception e) {
			logger.error("读取服务文件出错", e);
			throw new IllegalArgumentException("读取服务文件出错");
		}
	}

	/**
	 * 获取服务配置文件字节流，直接JSON转Micro对象
	 *
	 * @param resources
	 * @return
	 * @throws IOException
	 */
	private Micro readConf(Map<String, byte[]> resources) throws IOException {
		byte[] confBytes = resources.get("default.conf");
		if (confBytes == null) {
			throw new IllegalArgumentException("服务加载失败，配置文件找不到");
		}
		return JSON.parseObject(IOUtils.toString(confBytes, "UTF-8"), Micro.class);
	}

	/**
	 * 读取模型文件字节流，加载到RuleContainer，并注册ModelHandlerFactory
	 *
	 * @param micro
	 * @param resources
	 * @throws IOException
	 */
	private void readModel(Micro micro, Map<String, byte[]> resources) throws IOException {
		List<MicroDeployment.Model> models = micro.getDeployment().getModels();
		if (models.isEmpty()) {
			logger.warn("服务加载失败，模型文件为空: {}", micro);
			throw new IllegalArgumentException("服务加载失败，模型文件为空");
		}
		if (models.stream().noneMatch(m -> m.isPrimary())) {
			logger.warn("服务加载失败，生效模型文件找不到: {}", micro);
			throw new IllegalArgumentException("服务加载失败，生效模型文件找不到");
		}

		for (MicroDeployment.Model model : micro.getDeployment().getModels()) {
			ModelHandler handler = ModelHandlerFactory.get(model.getId());
			if (handler != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("模型已经部署：{}", model);
				}
				continue;
			}

			String modelName = resources.keySet().stream().filter(k -> k.startsWith(model.getId())).findFirst().orElse(null);
			if (StringUtils.isBlank(modelName)) {
				logger.warn("服务加载失败，模型文件找不到: {}", model);
				throw new IllegalArgumentException("服务加载失败，模型文件找不到");
			}

			byte[] modelBytes = resources.get(modelName);
			ModelHandler modelHandler;
			if (RestConstants.MicroType.RULE.equals(micro.getMicroType())) {
				modelHandler = new RuleModelHandler(model.getId(), modelBytes);
			} else {
				// 创建临时模型文件
				File tmpDir = SafelyFiles.newFile(System.getProperty("gaea.tmpdir"));
				FileUtils.forceMkdir(tmpDir);
				File jarFile = SafelyFiles.newFile(tmpDir, modelName);
				FileUtils.writeByteArrayToFile(jarFile, modelBytes);
				modelHandler = new AiModelHandler(jarFile);
			}

			// 注册到ModelHandlerFactory
			ModelHandlerFactory.register(model.getId(), modelHandler);
		}
	}

	/**
	 * 描述: 获取到期时间
	 *
	 * @param: [micro]
	 * @author: 周庚新
	 * @date: 2020/11/2
	 * @return: java.util.Date
	 */
	private Date getExpiredTime(Micro micro) {
		String expiredTimeKey = getExpiredTimeKey(micro);
		String expiredTime = stringRedisTemplate.boundValueOps(expiredTimeKey).get();
		if (StringUtils.isBlank(expiredTime)) {
			return null;
		}

		try {
			return DateUtils.parseDate(expiredTime, "yyyy-MM-dd HH:mm:ss");
		} catch (ParseException e) {
			logger.warn("解析到期时间失败. expiredTime:{}", expiredTime, e);
			return null;
		}
	}

	private String getIncomingQuantityKey(Micro micro) {
		return String.format(
				"micro:%s:deployment:%s:incomingQuantity",
				micro.getId(),
				micro.getDeployment().getId()
		);
	}

	private String getExpiredTimeKey(Micro micro) {
		return String.format("micro:%s:deployment:%s:expiredTime", micro.getId(), micro.getDeployment().getId());
	}

	private String getExecuteHistoryKey(Micro micro) {
		return String.format("micro:%s:deployment:%s:execute.history", micro.getId(), micro.getDeployment().getId());
	}

	private String getPercentKey(Micro micro) {
		return String.format("micro:%s:deployment:%s:percent", micro.getId(), micro.getDeployment().getId());
	}

	/**
	 * 直接执行生效模型
	 *
	 * @param micro
	 * @param input
	 * @return
	 */
	private Output doCommon(Micro micro, Input input) {
		MicroDeployment deployment = micro.getDeployment();
		List<MicroDeployment.Model> models = deployment.getModels();
		MicroDeployment.Model primaryModel = models.stream().filter(m -> m.isPrimary()).findFirst().get();
		return executeModel(micro, primaryModel, input);
	}

	/**
	 * 执行AB测试
	 *
	 * @param micro
	 * @param input
	 * @return
	 */
	private Output doABTest(Micro micro, Input input) {
		if (logger.isDebugEnabled()) {
			logger.debug("AB测试执行模型: micro: {}, input: {}", micro, input);
		}

		Output returnOutput = null;
		// 每个模型都执行，只返回生效模型的结果
		for (MicroDeployment.Model model : micro.getDeployment().getModels()) {
			if (model.isPrimary()) {
				returnOutput = executeModel(micro, model, input);
			} else {
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						try {
							executeModel(micro, model, input);
						} catch (Exception e) {
							logger.error("执行服务失败[AB测试模型]. input: {}, micro: {}", input, micro, e);
						}
					}
				});
			}
		}
		return returnOutput;
	}

	/**
	 * 执行冠军挑战者
	 *
	 * @param micro
	 * @param input
	 * @return
	 */
	private Output doChampionChallenger(Micro micro, Input input) {
		if (logger.isDebugEnabled()) {
			logger.debug("冠军挑战者执行模型: micro: {}, input: {}", micro, input);
		}

		String inputMd5 = getInputMd5(input);
		String executeHistoryKey = getExecuteHistoryKey(micro);
		MicroDeployment.Model model = selectModel(
				micro.getDeployment().getModels(),
				executeHistoryKey,
				inputMd5,
				micro
		);
		Output output = executeModel(micro, model, input);
		// 绑定此次输入和执行模型
		stringRedisTemplate.boundHashOps(executeHistoryKey).put(inputMd5, model.getId());
		return output;
	}

	/**
	 * 执行模型
	 *
	 * @param micro
	 * @param model
	 * @param input
	 * @return
	 */
	private Output executeModel(Micro micro, MicroDeployment.Model model, Input input) {
		ModelHandler handler = ModelHandlerFactory.get(model.getId());
		Output output = null;
		try {
			output = handler.handle(input);
			saveLog(micro, model, input, output);
			return output;
		} catch (Exception e) {
			if (e instanceof RuleException || e instanceof IllegalStateException) {
				saveFailLog(micro, model, input);
			}
			throw e;
		}
	}

	/**
	 * 描述: 保存执行失败的日志记录
	 *
	 * @param: [micro, model, input]
	 * @author: 周庚新
	 * @date: 2020/11/3
	 * @return: void
	 */
	private void saveFailLog(Micro micro, MicroDeployment.Model model, Input input) {
		DecisionLog decisionLog = initDecisionLog(micro, input);
		if (!MicroDeployment.DeployType.AB_TEST.equals(micro.getDeployment().getType()) || model.isPrimary()) {
			decisionLog.setEffective(true);
		} else {
			decisionLog.setEffective(false);
		}
		decisionLog.setState(DecisionLog.STATE_FAIL);
		decisionLog.setPkgUuid(model.getPkgUuid());
		decisionLog.setPkgBaseline(model.getPkgBaseline());
		if (input.isAsync()) {
			decisionLogService.update(decisionLog);
		} else {
			decisionLogService.add(decisionLog);
		}
	}

	/**
	 * 选择一个模型
	 *
	 * @param models
	 * @param executeHistoryKey
	 * @param inputMd5
	 * @param micro
	 * @return
	 */
	private MicroDeployment.Model selectModel(List<MicroDeployment.Model> models, String executeHistoryKey, String inputMd5, Micro micro) {
		/*
		 * 冠军挑战者按照流量选择模型，第一次概率随机，但是相同参数重复调用，对外不应该返回不同结果
		 * 所以先根据输入字符串的MD5值，查找历史是否执行过，如果找到直接用执行过的模型，否则利用权重挑选一个
		 * */
		String modelId = (String) stringRedisTemplate.boundHashOps(executeHistoryKey).get(inputMd5);
		if (modelId != null) {
			MicroDeployment.Model executedModel = models.stream().filter(m -> modelId.equals(m.getId())).findFirst().orElse(null);
			if (executedModel != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("重复调用模型：{}", executedModel);
				}
				return executedModel;
			}
		}

		Random random = new Random();
		int length = models.size(); // 总个数
		int totalWeight = 0; // 总权重
		boolean sameWeight = true; // 权重是否都一样
		for (int i = 0; i < length; i++) {
			setPercentFromRedis(models.get(i), micro);
			int weight = models.get(i).getPercent();
			totalWeight += weight; // 累计总权重
			if (sameWeight && i > 0 && weight != models.get(i - 1).getPercent()) {
				sameWeight = false; // 计算所有权重是否一样
			}
		}

		if (totalWeight > 0 && !sameWeight) {
			int offset = random.nextInt(totalWeight);
			for (int i = 0; i < length; i++) {
				offset -= models.get(i).getPercent();
				if (offset < 0) {
					return models.get(i);
				}
			}
		}

		// 如果权重都相同或者都为0则均等随机挑选
		return models.get(random.nextInt(length));
	}

	private void setPercentFromRedis(MicroDeployment.Model model, Micro micro) {
		String percentKey = getPercentKey(micro);
		String percent = (String) stringRedisTemplate.boundHashOps(percentKey).get(model.getId());
		if (percent != null) {
			model.setPercent(Integer.parseInt(percent));
		} else {
			model.setPercent(0);
		}
	}

	/**
	 * 计算输入参数的MD5值，标识是否相同输入重复调用
	 *
	 * @param input
	 * @return
	 */
	private String getInputMd5(Input input) {
		return EncodeUtils.encodeMD5(input.getData().toString());
	}

	private void saveLog(Micro micro, MicroDeployment.Model model, Input input, Output output) {
		DecisionLog decisionLog = initDecisionLog(micro, input);
		if (!MicroDeployment.DeployType.AB_TEST.equals(micro.getDeployment().getType()) || model.isPrimary()) {
			decisionLog.setEffective(true);
		} else {
			decisionLog.setEffective(false);
		}
		decisionLog.setPkgUuid(model.getPkgUuid());
		decisionLog.setPkgBaseline(model.getPkgBaseline());
		decisionLog.setState(DecisionLog.STATE_SUCCESS);

		Object res = output.getResponse();
		RuleEngine ruleEngine = output.getRuleEngine();

		if (res != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				decisionLog.setResText(objectMapper.writeValueAsString(res));
			} catch (Exception e) {
				logger.error("序列化失败：{}", output.toString(), e);
				decisionLog.setResText(StringUtils.EMPTY);
			}

			if (ruleEngine != null) {
				decisionLog.setFullText(JSON.toJSONString(ruleEngine.getObjectMap()));
			}
		}

		//设置执行轨迹
		if (ruleEngine != null) {
			setTrace(decisionLog, ruleEngine);
		}
		//设置审批类服务的审批结果
		setApprovalPass(decisionLog, micro, output);
		if (input.isAsync()) {
			decisionLogService.update(decisionLog);
		} else {
			decisionLogService.add(decisionLog);
		}
		output.setSeqNo(decisionLog.getSeqNo());
	}

	private void setTrace(DecisionLog decisionLog, RuleEngine ruleEngine) {
		ExecutionResult executionResult = ruleEngine.getExecutionResult();
		JSONObject decisionTrace = new JSONObject();
		decisionTrace.put("fireTime", executionResult.getExecutionTime());
		decisionTrace.put("fireNum", executionResult.getFireNum());
		decisionTrace.put("fireRules", executionResult.getFireRuleMetas());
		decisionTrace.put("fireNodes", executionResult.getFireNodeMap());
		decisionLog.setDecisionTrace(decisionTrace.toJSONString());
	}

	private void setApprovalPass(DecisionLog decisionLog, Micro micro, Output output) {
		ApprovalLabel approvalLabel = micro.getApprovalLabel();
		decisionLog.setPass(null);
		if (approvalLabel != null) {
			Object res = output.getResponse();
			Boolean pass = false;
			if (res != null) {
				JSONObject object = JSON.parseObject(JSONObject.toJSONString(res));
				JSONObject finalObj = null;
				String id = approvalLabel.getId();
				String label = approvalLabel.getField();
				JSONObject object2 = object.getJSONObject(id);
				if (object2 != null) {
					finalObj = object2;
				} else {
				    finalObj = object;
                }
				Object data = JSONPath.read(finalObj.toJSONString(), "$." + label);
				if (data != null) {
					JSONObject dataObj = new JSONObject();
					dataObj.put("data", data);
					Boolean flag = false;
					Integer dataInt = -1;
					try {
						flag = dataObj.getBoolean("data");
					} catch (Exception e) {
						flag = false;
					}
					try {
						dataInt = dataObj.getIntValue("data");
					} catch (Exception e) {
						dataInt = -1;
					}

					if ((flag != null && flag) || (dataInt != null && dataInt == 1)) {
						pass = true;
					}
				}
			}
			decisionLog.setPass(pass);
		}
	}

	private void register(Micro micro) {
		Micro oldMicro = MicroFactory.get(micro.getId());
		MicroFactory.register(micro);

		if (oldMicro != null) {
			// 旧的服务不为空，卸载旧的模型
			oldMicro.getDeployment().getModels().removeAll(micro.getDeployment().getModels());
			oldMicro.getDeployment().getModels().forEach(model -> {
				unloadModel(model);
				logger.info("卸载旧的模型. micro: {}, model: {}", micro.getId(), model.getId());
			});
		}
	}

	private void unloadModel(MicroDeployment.Model model) {
		ByteArrayRuleContainer.removeRule(model.getId());
		ModelHandlerFactory.remove(model.getId());
	}

	private void removeRedisConfs(Micro micro) {
		String incomingQuantityKey = getIncomingQuantityKey(micro);
		String expireTimeKey = getExpiredTimeKey(micro);
		String percentKey = getPercentKey(micro);
		String executeHistoryKey = getExecuteHistoryKey(micro);
		stringRedisTemplate.delete(Arrays.asList(incomingQuantityKey, expireTimeKey, percentKey, executeHistoryKey));
	}
}
