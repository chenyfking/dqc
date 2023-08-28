package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.common.Result;
import com.beagledata.gaea.common.OkHttpClientFactory;
import com.beagledata.gaea.common.RestConstants;
import com.beagledata.gaea.ruleengine.util.PackageUtils;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.ruleengine.util.ZipUtils;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.config.DefaultConfigs;
import com.beagledata.gaea.workbench.entity.*;
import com.beagledata.gaea.workbench.mapper.*;
import com.beagledata.gaea.workbench.rule.builder.KnowledgePackageRuleBuilder;
import com.beagledata.gaea.workbench.rule.define.Fact;
import com.beagledata.gaea.workbench.service.ClientService;
import com.beagledata.gaea.workbench.service.KnowledgePackageService;
import com.beagledata.gaea.workbench.service.MicroService;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Chenyafeng on 2018/12/3.
 */
@Service
public class MicroServiceImpl extends BaseServiceImpl implements MicroService {
	@Autowired
	private MicroMapper microMapper;
	@Autowired
	private KnowledgePackageMapper knowledgePackageMapper;
	@Autowired
	private MicroRelationMapper microRelationMapper;
	@Autowired
	private AiModelMapper aiModelMapper;
	@Autowired
	private ResourceResolver resourceResolver;
	@Autowired
	private ClientMapper clientMapper;
	@Autowired
	private ClientService clientService;
	@Autowired
	private ExecuteRecordMapper executeRecordMapper;
	@Autowired
	private DefaultConfigs defaultConfigs;
	@Autowired
	private KnowledgePackageService knowledgePackageService;
	@Autowired
	private AssetsMapper assetsMapper;
	@Autowired
	private KnowledgePackageBaselineMapper knowledgePackageBaselineMapper;

	/**
	 * 同步保存知识包使用的AI模型
	 */
	private void saveMicroModels(Set<String> modelNames, String microUuid) {
		//删除已有的模型关联
		microRelationMapper.deleteModels(microUuid);

		if (modelNames.isEmpty()) {
			return;
		}

		List<AiModel> models = aiModelMapper.selectByModelNames(modelNames);
		List<MicroModel> microModels = new ArrayList<>(models.size());
		for (AiModel model : models) {
			MicroModel microModel = new MicroModel(microUuid, model.getUuid());
			microModels.add(microModel);
		}
		if (!microModels.isEmpty()) {
			microRelationMapper.batchInsertOrUpdateModel(microModels);
		}
	}

	@Override
	public Result listPage(Micro micro, int page, int pageNum) {
		try {
			boolean isAdmin = UserHolder.hasAdminPermission();
			boolean isOrgAdmin = UserHolder.isOrgAdmin();
			String userUuid = UserHolder.currentUserUuid();
			int total = microMapper.countTotal(micro, isAdmin, isOrgAdmin, userUuid);
			if (total > 0) {
				List<Micro> list = microMapper.selectPage(
						micro, isAdmin, isOrgAdmin, userUuid, (page - 1) * pageNum, pageNum
				);
				return Result.newSuccess().withTotal(total).withData(list);
			}
		} catch (Exception e) {
			logger.error("查询服务列表失败. page: {}, pageNum: {}, micro: {}", page, pageNum, micro, e);
		}
		return Result.emptyList();
	}

	@Override
	public List<KnowledgePackageBaseline> listBaselineForDeploy(String microUuid) {
		try {
			return knowledgePackageBaselineMapper.selectByMicro(microUuid);
		} catch (Exception e) {
			logger.error("查询可以上线的基线列表失败: {}", microUuid, e);
		}
		return Collections.emptyList();
	}

	@Override
	public List<Micro> listAllForToken() {
		try {
			return microMapper.selectAll(
						UserHolder.hasAdminPermission(), UserHolder.isOrgAdmin(), UserHolder.currentUserUuid()
			);
		} catch (Exception e) {
			logger.error("查询服务列表失败", e);
		}
		return Collections.emptyList();
	}

	@Override
	public Result refusePass(KnowledgePackage knowledgePackage) {
		if (StringUtils.isBlank(knowledgePackage.getUuid()))
			throw new IllegalArgumentException("参数不能为空");
		try {
			KnowledgePackage knowledgePackage1 = knowledgePackageMapper.selectByUuid(knowledgePackage.getUuid());
			if (null == knowledgePackage1) {
				throw new IllegalArgumentException("该知识包不存在！");
			} else {
//				if (knowledgePackage1.getAuditStatus() == 0 || knowledgePackage1.getAuditStatus() == 1) {
//					//更新知识包的状态为未通过 auditStatus = 3
//					knowledgePackage.setAuditStatus(3);
//					knowledgePackageMapper.update(knowledgePackage);
//					return Result.newSuccess();
//				} else {
//					throw new IllegalArgumentException("该知识包已经通过审批！");
//				}
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	@Override
	public Result enableMicro(String uuid, Boolean enable) {
		if (StringUtils.isBlank(uuid) || enable == null)
			throw new IllegalArgumentException("参数不能为空");
		try {
			microMapper.updateEnable(uuid, enable);
			return Result.newSuccess();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e, "启用或停用服务失败: uuid = " + uuid);
			throw e;
		}
	}

	@Override
	public Result updateMicro(Micro micro) {
		if (micro == null || StringUtils.isBlank(micro.getUuid()))
			throw new IllegalArgumentException("参数不能为空");
		try {
			Micro micro1 = microMapper.selectByUuid(micro.getUuid());
			if (micro1 == null) {
				throw new IllegalArgumentException("该服务不存在！");
			}
			microMapper.update(micro);
			return Result.newSuccess();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e, "更新服务失败: uuid = " + micro.getUuid());
			throw e;
		}
	}

	@Override
	@Transactional
	public void deleteMicro(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			throw new IllegalArgumentException("参数不能为空");
		}

		try {
			//逻辑删除服务
			int rows = microMapper.delete(uuid);
			if (rows <= 0) {
				logger.warn("删除服务失败: {}", uuid);
				return;
			}

			List<Client> clients = clientMapper.selectByMicro(uuid);
			if (clients.isEmpty()) {
				return;
			}

			// 删除所有关联集群节点的服务
			for (Client client : clients) {
				clientService.unDeploy(client.getBaseUrl(), uuid);
			}
		} catch (Exception e) {
			logger.error(uuid + ", " + e.getLocalizedMessage(), e);
			throw new IllegalStateException("删除服务失败");
		}
	}

	@Override
	public Result getApiDoc(String microUuid) {
		try {
			List<Client> clients = clientMapper.selectByMicro(microUuid);
			if (clients.isEmpty()) {
				logger.warn("获取服务API调用文档失败, 未找到可用的集群节点. microUuid: {}", microUuid);
				return Result.newError().withMsg("未找到可用的集群节点");
			}

			// 调用第一个可用集群节点的文档接口
			Client client = clients.stream()
					.filter(cli -> !Optional.ofNullable(cli.getDisabled()).orElse(false))
					.findFirst().orElse(null);
			if (client == null) {
				logger.warn("获取服务API调用文档失败, 未找到可用的集群节点. microUuid: {}, clients: {}", microUuid, clients);
				return Result.newError().withMsg("未找到可用的集群节点");
			}

			String url = StringUtils.concatUrl(client.getBaseUrl(), RestConstants.OpenApi.Endpoints.APIDOC);
			url += "?uuid=" + microUuid;
			OkHttpClient httpClient = OkHttpClientFactory.get();
			Request request = new Request.Builder()
					.url(url)
					.build();
			try (Response response = httpClient.newCall(request).execute()) {
				if (!response.isSuccessful()) {
					logger.warn("获取服务API调用文档失败, microUuid: {}, url: {}, 接口报错: {}", microUuid, url, response);
					return Result.newError().withMsg("获取接口调用文档失败");
				}

				JSONObject result = JSON.parseObject(response.body().string());
				if (result.getIntValue("code") != 0) {
					logger.warn("获取服务API调用文档失败, microUuid: {}, url: {}, 返回结果: {}", microUuid, url, result);
					return Result.newError().withMsg(Optional.ofNullable(result.getString("msg")).orElse("获取接口调用文档失败"));
				}

				JSONObject data = result.getJSONObject("data");
				data.put("serviceUrl", client.getBaseUrl() + "/execute/" + microUuid);
				return Result.newSuccess().withData(data);
			}
		} catch (Exception e) {
			logger.error(microUuid + ", " + e.getLocalizedMessage(), e);
			return Result.newError().withMsg("获取接口调用文档失败");
		}
	}

	@Override
	public Result listPageExecuteRecords(String microUuid, int page, int pageNum, Date startTime, Date endTime) {
		try {
			int total = executeRecordMapper.countByMicro(microUuid, startTime, endTime);
			if (total <= 0) {
				return Result.newSuccess().withData(Collections.emptyList());
			}

			List<ExecuteRecord> list = executeRecordMapper.selectByMicro(microUuid, (page - 1) * pageNum, pageNum, startTime, endTime);
			return Result.newSuccess().withData(list).withTotal(total);
		} catch (Exception e) {
			logger.error(microUuid + ", " + e.getLocalizedMessage(), e);
		}
		return Result.newSuccess();
	}

	private void validateMicro(Micro micro) {
		if (StringUtils.isBlank(micro.getPackageUuid())) {
			logger.warn("校验服务失败: {}", micro);
			throw new IllegalArgumentException("知识包无效");
		}
		if (StringUtils.isBlank(micro.getName()) || micro.getName().length() > 30) {
			logger.warn("校验服务失败: {}", micro);
			throw new IllegalArgumentException("服务名称不能为空并且长度不能超过30个字符");
		}
		if (micro.getClients().isEmpty()) {
			logger.warn("校验服务失败: {}", micro);
			throw new IllegalArgumentException("请选择要发布的集群节点");
		}
	}

	private KnowledgePackage getKnowledgePackage(Micro micro) {
		KnowledgePackage knowledgePackage = knowledgePackageMapper.selectByUuid(micro.getPackageUuid());
		if (knowledgePackage == null) {
			logger.warn("获取知识包失败: {}", micro.getPackageUuid());
			throw new IllegalArgumentException("知识包不存在");
		}
		List<Assets> packageAssets = knowledgePackageMapper.selectAssetsWithContent(micro.getPackageUuid());
		if (packageAssets.isEmpty()) {
			logger.warn("获取知识包失败: {}", micro.getPackageUuid());
			throw new IllegalArgumentException("空知识包不能创建服务");
		}

		knowledgePackage.getAssetsList().addAll(packageAssets);
		return knowledgePackage;
	}

	/**
	* 描述: 根据知识包uuid 基线版本号 获取资源文件生成知识包
	* @param: [pakUuid, bselineVersion]
	* @author: 周庚新
	* @date: 2020/7/3
	* @return: com.beagledata.gaea.workbench.entity.KnowledgePackage
	*
	*/
	private KnowledgePackage getKnowledgePackage(String packgeUuid, Integer bselineVersion) {
		KnowledgePackage knowledgePackage = knowledgePackageMapper.selectByUuid(packgeUuid);
		if (knowledgePackage == null) {
			logger.warn("获取知识包失败: {}", packgeUuid);
			throw new IllegalArgumentException("知识包不存在");
		}
		List<Assets> packageAssets = knowledgePackageMapper.selectAssetsVersionWithContent(packgeUuid, bselineVersion);
		if (packageAssets.isEmpty()) {
			logger.warn("获取知识包失败: packgeUuid {} ,baselineVersion {}", packgeUuid,bselineVersion);
			throw new IllegalArgumentException("空知识包不能创建服务");
		}
		knowledgePackage.getAssetsList().addAll(packageAssets);
		return knowledgePackage;
	}

	private void getClients(Micro micro) {
		List<Client> clients = clientMapper.selectByUuids(micro.getClients());
		if (clients.isEmpty()) {
			logger.warn("获取集群节点列表失败: {}", micro.getClients());
			throw new IllegalArgumentException("集群节点无效");
		}

		micro.setClients(clients);
	}

	private void saveMicro(Micro micro, KnowledgePackage knowledgePackage) {
		micro.setUuid(IdUtils.UUID());
		microMapper.insert(micro);
		// 保存服务集群节点关联
		microRelationMapper.insertBatchMicroClientRoute(micro);
		// 保存服务知识包关联
		knowledgePackage.setMicroUuid(micro.getUuid());
		knowledgePackageMapper.update(knowledgePackage);
	}

	private KnowledgePackageRuleBuilder getRuleBuilder(Micro micro, KnowledgePackage knowledgePackage) throws Exception {
		KnowledgePackageRuleBuilder ruleBuilder = KnowledgePackageRuleBuilder.newBuilder(knowledgePackage);
		// 设置服务的uuid, 作为调用的service标识
		ruleBuilder.setId(micro.getUuid());
		ruleBuilder.buildAll();
		return ruleBuilder;
	}

	private void deployRule2Clients(KnowledgePackageRuleBuilder ruleBuilder, Micro micro) throws IOException {
		byte[] bytes = ruleBuilder.getBytes();
		// 把知识包推到每个集群节点
		for (Client client : micro.getClients()) {
			clientService.deployRule(client.getBaseUrl(), micro.getUuid(), bytes);
		}
	}

	@Override
	public Result approvalData(String uuid) {
		KnowledgePackageBaseline baseline = microMapper.selectLeastBaseline(uuid);
		if (baseline == null) {
			throw new IllegalArgumentException("服务不存在");
		}

		List<Fact> facts = null;
		File baselineFile = SafelyFiles.newFile(
				resourceResolver.getPkgBaselinePath()
						+ File.separator
						+ baseline.getPackageUuid()
						+ "_"
						+ baseline.getVersionNo()
						+ ".zip"
		);
		if (baselineFile.exists()) {
			try {
				byte[] bytes = PackageUtils.decrypt(FileUtils.readFileToByteArray(baselineFile));
				Map<String, byte[]> resource = ZipUtils.decompression(new ByteArrayInputStream(bytes));
				JSONObject confJson = JSON.parseObject(IOUtils.toString(resource.get("default.conf"), "UTF-8"));
				facts = JSON.parseArray(confJson.get("approvalFacts").toString(), Fact.class);
			} catch (IOException e) {
				logger.error("解析本地基线包失败: {}", baselineFile);
				throw new IllegalArgumentException("获取失败");
			}
		} else {
			facts = knowledgePackageService.getTestData(baseline.getPackageUuid(), baseline.getVersionNo());
		}

		JSONArray array = new JSONArray();
		if (facts != null && !facts.isEmpty()) {
			for (Fact fact : facts) {
				JSONObject factJson = JSON.parseObject(JSON.toJSONString(fact));
				getChildren(factJson, array, null, true);
			}
		}
		return Result.newSuccess().withData(array);
	}

	private void getChildren(JSONObject factJson, JSONArray array, JSONObject parentFactJson, boolean add) {
		JSONArray fields = factJson.getJSONArray("fields");
		JSONArray fieldArray = new JSONArray();
		for (int i = 0; i < fields.size(); i++) {
			JSONObject fieldJson = fields.getJSONObject(i);
			if (parentFactJson != null) {
				String id = parentFactJson.getString("id") + fieldJson.getString("id");
				fieldJson.put("id", id);
				fieldJson.put("approvalLabel", parentFactJson.getString("approvalLabel") + "-" + fieldJson.getString("label"));
				fieldJson.put("approvalName", parentFactJson.getString("approvalName") + "-" + fieldJson.getString("name"));
			} else {
				fieldJson.put("approvalLabel", fieldJson.getString("label"));
				fieldJson.put("approvalName", fieldJson.getString("name"));
			}

			String subType = fieldJson.getString("subType");
			if (StringUtils.isNotBlank(subType) && subType.length() == 32) {
				Assets assets = assetsMapper.selectByUuid(subType);
				Fact subFact = Fact.fromAssets(assets);
				JSONObject subFactJson = JSON.parseObject(JSONObject.toJSONString(subFact));
				getChildren(subFactJson, array, fieldJson, false);
				fieldJson.put("children", subFactJson.getJSONArray("fields"));
			}
			fieldArray.add(fieldJson);
		}
		factJson.put("fields", fieldArray);
		if (add) {
			array.add(factJson);
		}
	}
}
