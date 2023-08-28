package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.common.Result;
import com.beagledata.gaea.common.RestConstants;
import com.beagledata.gaea.ruleengine.util.PackageUtils;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.ruleengine.util.ZipUtils;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.entity.*;
import com.beagledata.gaea.workbench.mapper.*;
import com.beagledata.gaea.workbench.rule.builder.KnowledgePackageRuleBuilder;
import com.beagledata.gaea.workbench.service.ClientService;
import com.beagledata.gaea.workbench.service.MicroDeploymentService;
import com.beagledata.gaea.workbench.service.MicroRouteService;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @Author yangyongqiang
 **/
@Service
public class MicroDeploymentServiceImpl implements MicroDeploymentService {
	private static Logger logger = LoggerFactory.getLogger(MicroDeploymentServiceImpl.class);
	@Autowired
	private MicroDeploymentMapper microDeploymentMapper;
	@Autowired
	private KnowledgePackageBaselineMapper baselineMapper;
	@Autowired
	private ClientMapper clientMapper;
	@Autowired
	private KnowledgePackageMapper knowledgePackageMapper;
	@Autowired
	private MicroRelationMapper microRelationMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private ClientService clientService;
	@Autowired
	private ResourceResolver resourceResolver;
	@Autowired
	private MicroRouteService microRouteService;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private MicroMapper microMapper;

	/**
	 * 模型上线部署（A/B测试及冠军挑战者测试）
	 * @author yinrj
	 * @date 2020/7/14
	 */
	@Override
	@Transactional
	public Result deploy(MicroDeployment deployment) {
		validateDeploment(deployment);

		if (StringUtils.isBlank(deployment.getUuid())) {
			deployment.setUuid(IdUtils.UUID());
		} else {
			// 只有AB测试和冠军挑战者可以编辑，不需要重新打包，直接修改配置参数
			// 更新上线记录
			microDeploymentMapper.insertOrUpdate(deployment);
			microDeploymentMapper.insertModel(deployment);
			saveConfsToRedis(deployment);
			return Result.newSuccess();
		}

		// 查询可以推送部署的集群节点
		List<Client> clients = clientMapper.selectAllAvailable();
		if (clients.isEmpty()) {
			logger.warn("上线失败，没有可用的集群节点: {}", deployment);
			throw new IllegalArgumentException("上线失败，没有可用的集群节点");
		}

		try {
			// 制作服务包
			byte[] microBytes = getMicroPackage(deployment);
			// 服务包推送到所有集群节点
			boolean deploySuccess = deploy2Clients(microBytes, clients, deployment.getMicroUuid());
			if (!deploySuccess) {
				// 集群节点全部推送失败
				throw new IllegalStateException("生效上线失败，所有集群节点都没有推送成功");
			}
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			logger.error("生效上线失败: {}", deployment, e);
			throw new IllegalStateException("生效上线失败");
		}

		try {
			for (MicroDeployment.Model model : deployment.getModels()) {
				if (model.isPrimary()) {
					// 已生效基线改成待生效，当前模型改成已生效
					baselineMapper.online(model.getPkgBaseline().getPackageUuid(), model.getPkgBaseline().getVersionNo());
					break;
				}
			}
			// 保存上线记录
			microDeploymentMapper.insertOrUpdate(deployment);
			// 保存上线记录和知识包关联记录
			microDeploymentMapper.insertModel(deployment);
			// AB测试和冠军挑战者配置缓存到redis
			saveConfsToRedis(deployment);
		} catch (Exception e) {
			logger.error("生效上线失败: {}", deployment, e);
			throw new IllegalStateException("生效上线失败");
		}

		logger.info("服务上线成功: {}", deployment);
		return Result.SUCCESS;
	}


	@Override
	public byte[] getMicroPackage(MicroDeployment deployment) throws Exception {
		Map<String, byte[]> resource = new HashMap<>();

		for (MicroDeployment.Model model : deployment.getModels()) {
			// 编译每个基线知识包
			KnowledgePackageBaseline baseline = model.getPkgBaseline();
			byte[] bytes = getBaselineBytes(baseline.getPackageUuid(), baseline.getVersionNo());
			resource.put(baseline.getPackageUuid() + "_" + baseline.getVersionNo() + ".zip", bytes);
		}

		// 编写服务包配置文件
		byte[] confBytes = getConfBytes(deployment);
		resource.put("default.conf", confBytes);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipUtils.compression(baos, resource);
		byte[] microBytes = PackageUtils.encrypt(baos.toByteArray());
		cacheMicroPackage(deployment.getMicroUuid(), microBytes);
		return microBytes;
	}

	private void cacheMicroPackage(String microUuid, byte[] microBytes) {
		// 缓存服务文件到磁盘
		File file = SafelyFiles.newFile(resourceResolver.getDeployPath(), microUuid + ".zip");
		try {
			FileUtils.writeByteArrayToFile(file, microBytes);
		} catch (IOException e) {
			logger.warn("缓存服务文件到磁盘失败: {}", file, e);
		}
	}

	private byte[] getBaselineBytes(String pkgUuid, Integer version) throws Exception {
		String id = pkgUuid + "_" + version;
		File baselineFile = SafelyFiles.newFile(resourceResolver.getPkgBaselinePath(), id + ".zip");
		if (baselineFile.exists()) {
			// 先从硬盘缓存找
			return PackageUtils.decrypt(FileUtils.readFileToByteArray(baselineFile));
		}

		// 硬盘没有找到从数据库查
		KnowledgePackage pkg = selectByUuidWithAssets(pkgUuid, version);
		if (pkg == null) {
			throw new IllegalArgumentException("知识包不存在");
		}

		KnowledgePackageRuleBuilder ruleBuilder = KnowledgePackageRuleBuilder.newBuilder(pkg);
		ruleBuilder.setId(id);
		ruleBuilder.buildAll();
		return ruleBuilder.getBytes();
	}

	private KnowledgePackage selectByUuidWithAssets(String uuid, Integer baseline) {
		KnowledgePackage pkg = knowledgePackageMapper.selectByUuid(uuid);
		if (pkg != null) {
			pkg.setBaselineVersion(baseline);
			pkg.setAssetsList(knowledgePackageMapper.selectAssetsVersionWithContent(pkg.getUuid(), baseline));
		}
		return pkg;
	}

	@Override
	public Result listPage(int page, int pageNum, String microUuid) {
		if (StringUtils.isBlank(microUuid)) {
			throw new IllegalArgumentException("服务参数为空！");
		}

		try {
			int count = microDeploymentMapper.countByMicroUuid(microUuid);
			if (count > 0) {
				List<MicroDeployment> deployments = microDeploymentMapper.selectByMicroUuid(
						microUuid, pageNum * (page - 1), pageNum
				);
				return Result.newSuccess().withData(deployments).withTotal(count);
			}
		} catch (Exception e) {
			logger.error("查询服务上线列表失败. page: {}, pageNum: {}, microUuid: {}", page, pageNum, microUuid, e);
		}
		return Result.emptyList();
	}

	@Override
	public List<MicroDeployment> listAll() {
		List<MicroDeployment> list = null;
		try {
			list = microDeploymentMapper.selectAll();
		} catch (Exception e) {
			list = Collections.emptyList();
		}
		return list;
	}

	@Override
	@Transactional
	public void offline(String microUuid, String packageUuid) {
		if (StringUtils.isBlank(microUuid)) {
			throw new IllegalArgumentException("决策服务uuid不能为空");
		}
		if (StringUtils.isBlank(packageUuid)) {
			throw new IllegalArgumentException("知识包uuid不能为空");
		}

		try {
			List<Client> clients = clientMapper.selectByMicro(microUuid);
			if (clients.isEmpty()) {
				return;
			}

			for (Client client : clients) {
				clientService.unDeploy(client.getBaseUrl(), microUuid);
			}
			microRelationMapper.deleteByMicroUuid(microUuid);
			baselineMapper.updateStateByNewState(packageUuid, Constants.BaselineStat.UNEFFECTIVE, Constants.BaselineStat.EFFECTIVE);
			File file = SafelyFiles.newFile(resourceResolver.getDeployPath(), microUuid + ".zip");
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			logger.error("下线决策服务失败: {}", microUuid, e);
			throw new IllegalStateException("下线决策服务失败");
		}
	}

	private void validateDeploment(MicroDeployment microDeployment) {
		if (microDeployment == null) {
			throw new IllegalArgumentException("参数为空！");
		}
		if (StringUtils.isBlank(microDeployment.getMicroUuid())) {
			throw new IllegalArgumentException("服务uuid为空！");
		}
		if (microDeployment.getType() == null) {
			throw new IllegalArgumentException("上线方式为空！");
		}
		if (!MicroDeployment.DeployType.COMMON.equals(microDeployment.getType())
				&& microDeployment.getIncomingQuantity() <= 0 && microDeployment.getExpiredTime() == null) {
			throw new IllegalArgumentException("最大进件量或到期时间至少填写一项！");
		}
		if (microDeployment.getModels().isEmpty()) {
			throw new IllegalArgumentException("上线模型为空！");
		}
		for (MicroDeployment.Model m : microDeployment.getModels()) {
			if (MicroDeployment.DeployType.CHAMPION_CHALLENGER.equals(microDeployment.getType())) {
				if (m.getPercent() == null) {
					throw new IllegalArgumentException("生效流量或测试流量为空！");
				}
			}
			if (m.getPkgBaseline() == null || StringUtils.isBlank(m.getPkgBaseline().getPackageUuid())) {
				throw new IllegalArgumentException("传入模型知识包为空！");
			}
			if (m.getPkgBaseline().getVersionNo() == null) {
				throw new IllegalArgumentException("传入模型版本号为空！");
			}
		}
	}

	private boolean deploy2Clients(byte[] microBytes, List<Client> clients, String microUuid) {
		List<String> deployFailClients = new ArrayList<>();
		boolean deploySuccess = false;
		// 把服务包推到每个集群节点
		for (Client client : clients) {
			try {
				clientService.deployRule(client.getBaseUrl(), microUuid, microBytes);
				// 上线成功一个则保持一个集群节点和服务的路由记录
				microRouteService.saveMicroRoute(client.getUuid(), microUuid);
				deploySuccess = true;
			} catch (Exception e) {
				deployFailClients.add(client.getUuid());
				logger.warn("服务部署到集群节点失败. microUuid: {}, clientUrl: {}", microUuid, client.getBaseUrl(), e);
			}
		}

		if (deploySuccess) {
			// 如果有一个上线成功，则删除上线不成功的模型路由
			for (String clientUuid : deployFailClients) {
				microRouteService.deleteMicroRoute(clientUuid, microUuid);
			}
		}
		return deploySuccess;
	}

	private boolean upgradedeploy2Clients(List<Client> clients, String microUuid) {
		List<String> deployFailClients = new ArrayList<>();
		boolean deploySuccess = false;
		// 把服务包推到每个集群节点
		for (Client client : clients) {
			try {
				// 上线成功一个则保持一个集群节点和服务的路由记录
				microRouteService.saveMicroRoute(client.getUuid(), microUuid);
				deploySuccess = true;
			} catch (Exception e) {
				deployFailClients.add(client.getUuid());
				logger.warn("服务部署到集群节点失败. microUuid: {}, clientUrl: {}", microUuid, client.getBaseUrl(), e);
			}
		}

		if (deploySuccess) {
			// 如果有一个上线成功，则删除上线不成功的模型路由
			for (String clientUuid : deployFailClients) {
				microRouteService.deleteMicroRoute(clientUuid, microUuid);
			}
		}
		return deploySuccess;
	}

	private byte[] getConfBytes(MicroDeployment deployment) {
		JSONObject conf = new JSONObject();
		conf.put("id", deployment.getMicroUuid());
		conf.put("deployTime", System.currentTimeMillis());
		conf.put("microType", RestConstants.MicroType.RULE);

		JSONObject deploymentConf = new JSONObject();
		deploymentConf.put("id", deployment.getUuid());
		deploymentConf.put("type", deployment.getType());
		deploymentConf.put("incomingQuantity", deployment.getIncomingQuantity());
		if (deployment.getExpiredTime() != null) {
			deploymentConf.put("expiredTime", deployment.getExpiredTime().getTime());
		}

		JSONArray models = new JSONArray();
		for (MicroDeployment.Model model : deployment.getModels()) {
			JSONObject object = new JSONObject();
			if (model.getPkgBaseline() != null) {
				object.put("id", model.getPkgBaseline().getPackageUuid() + "_" + model.getPkgBaseline().getVersionNo());
				object.put("percent", model.getPercent());
				object.put("primary", model.isPrimary());
			}
			models.add(object);
		}
		deploymentConf.put("models", models);
		conf.put("deployment", deploymentConf);

		Micro micro = microMapper.selectByUuid(deployment.getMicroUuid());
		if (micro != null) {
			conf.put("approvalLabel", JSON.parseObject(micro.getApprovalLabel()));
		}

		KnowledgePackage knowledgePackage = knowledgePackageMapper.selectByUuid(deployment.getModels().get(0).getPkgBaseline().getPackageUuid());
		if (knowledgePackage != null && knowledgePackage.getCreator() != null) {
			JSONObject owner = new JSONObject();
			User creator = knowledgePackage.getCreator();
			owner.put("userUuid", creator.getUuid());
			owner.put("userName", creator.getUsername());
			User orgUser = userMapper.selectOrgByUuid(creator.getUuid());
			if (orgUser != null && orgUser.getOrg() != null) {
				owner.put("orgUuid", orgUser.getOrg().getUuid());
				owner.put("orgName", orgUser.getOrg().getName());
			}
			conf.put("owner", owner);
		}

		return conf.toJSONString().getBytes();
	}

	private void saveConfsToRedis(MicroDeployment deployment) {
		if (MicroDeployment.DeployType.COMMON.equals(deployment.getType())) {
			return;
		}

		if (deployment.getIncomingQuantity() > 0) {
			// 最大进件量大于0，缓存redis
			String incomingQuantityKey = String.format(
					"micro:%s:deployment:%s:incomingQuantity",
					deployment.getMicroUuid(),
					deployment.getUuid()
			);
			stringRedisTemplate.boundValueOps(incomingQuantityKey).set(String.valueOf(deployment.getIncomingQuantity()));
		}

		if (deployment.getExpiredTime() != null) {
			// 到期时间不为空，缓存redis
			String expiredTimeKey = String.format(
					"micro:%s:deployment:%s:expiredTime",
					deployment.getMicroUuid(),
					deployment.getUuid()
			);
			stringRedisTemplate.boundValueOps(expiredTimeKey).set(
					DateFormatUtils.format(deployment.getExpiredTime(), "yyyy-MM-dd HH:mm:ss")
			);
		}

		if (MicroDeployment.DeployType.CHAMPION_CHALLENGER.equals(deployment.getType())) {
			// 冠军挑战者流程，缓存redis
			String percentKey = String.format(
					"micro:%s:deployment:%s:percent",
					deployment.getMicroUuid(),
					deployment.getUuid()
			);
			BoundHashOperations boundHashOperations = stringRedisTemplate.boundHashOps(percentKey);
			for (MicroDeployment.Model model : deployment.getModels()) {
				boundHashOperations.put(
						model.getPkgBaseline().getPackageUuid() + "_" + model.getPkgBaseline().getVersionNo(),
						String.valueOf(model.getPercent())
				);
			}
		}
	}
}
