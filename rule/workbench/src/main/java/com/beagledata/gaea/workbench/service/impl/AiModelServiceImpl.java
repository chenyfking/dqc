package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.beagledata.common.Result;
import com.beagledata.gaea.common.RestConstants;
import com.beagledata.gaea.ruleengine.aimodel.Invoker;
import com.beagledata.gaea.ruleengine.aimodel.JarInvoker;
import com.beagledata.gaea.ruleengine.aimodel.ModelMetadata;
import com.beagledata.gaea.ruleengine.aimodel.PmmlInvoker;
import com.beagledata.gaea.ruleengine.util.PackageUtils;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.ruleengine.util.ZipUtils;
import com.beagledata.gaea.workbench.common.BizCode;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.entity.*;
import com.beagledata.gaea.workbench.mapper.*;
import com.beagledata.gaea.workbench.service.AiModelService;
import com.beagledata.gaea.workbench.service.ClientService;
import com.beagledata.gaea.workbench.service.MicroRouteService;
import com.beagledata.gaea.workbench.service.MicroService;
import com.beagledata.gaea.workbench.util.ModelUtils;
import com.beagledata.gaea.workbench.util.PoiUtil;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Created by liulu on 2018/1/16.
 */
@Service
public class AiModelServiceImpl extends BaseServiceImpl implements AiModelService {
	private static URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	private static Method addURL = null;

	@Autowired
	private AiModelMapper aiModelMapper;
	@Autowired
	private MicroRelationMapper microRelationMapper;
	@Autowired
	private MicroMapper microMapper;
	@Autowired
	private ClientMapper clientMapper;
	@Autowired
	private MicroService microService;
	@Autowired
	private ResourceResolver resourceResolver;
	@Autowired
	private ClientService clientService;
	@Autowired
	private MicroDeploymentMapper microDeploymentMapper;
	@Autowired
	private MicroRouteService microRouteService;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private KnowledgePackageMapper knowledgePackageMapper;
	@Autowired
	private ReferMapper referMapper;


	static {
		try {
			addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			addURL.setAccessible(true);
		} catch (NoSuchMethodException ignore) {
		}
	}

	@Override
	public void initModelAction() {
		List<AiModel> models = aiModelMapper.selectAll(null, null, true, null, null, null);
		if (models.isEmpty()) {
			return;
		}

		try {
			for (AiModel model : models) {
				if (!model.isEnable()) {
					continue;
				}

				String modelPath = resourceResolver.getModelPath();
				String jarName = model.getJarName();
				File modelFile = SafelyFiles.newFile(modelPath, jarName);
				if (modelFile.exists()) {
					loadModelJar(modelFile);
				}
			}
		} catch (Exception e) {
			logger.error("初始化模型方法失败", e);
			throw new IllegalStateException();
		}
	}

	@Override
	@Transactional
	public void add(AiModel model) {
		validateModel(model);

		try {
			List<AiModel> dbModels = aiModelMapper.selectByModelNameOrJarName(model.getModelName(), model.getJarName());
			if (!dbModels.isEmpty()) {
				logger.warn("上传AI模型已经存在: {}", model);
				if (model.getModelName().equals(dbModels.get(0).getModelName())) {
					throw new IllegalArgumentException("模型名称不能重复");
				}
				throw new IllegalArgumentException("模型文件不能重复");
			}

			// 保存文件流到磁盘上
			saveModelFile(model);
			if (isGaeaModel(model.getJarName())) {
				// 解析参数
				model.setParams(new ModelMetadata(model.getDiskFile()).getParamsJson());
				model.setEnable(true);
			} else {
				model.setEnable(false);
			}
			model.setUuid(IdUtils.UUID());
			aiModelMapper.insert(model);

			// 加载模型到classpath
			loadModelJar(model.getDiskFile());
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("添加AI模型失败: {}", model, e);
			throw new IllegalStateException("添加失败");
		}
	}

	@Override
	@Transactional
	public void edit(AiModel model) {
		validateModel(model);

		try {
			List<AiModel> dbModels = aiModelMapper.selectByModelNameOrJarName(model.getModelName(), model.getJarName());
			if (!dbModels.isEmpty()) {
				for (AiModel dbModel : dbModels) {
					if (!dbModel.getUuid().equals(model.getUuid())) {
						logger.warn("上传AI模型已经存在: {}", model);
						if (model.getModelName().equals(dbModel.getModelName())) {
							throw new IllegalArgumentException("模型名称不能重复");
						}
						throw new IllegalArgumentException("模型文件不能重复");
					}
				}
			}

			if (model.getFormFile() != null) {
				// 保存文件流到磁盘上
				saveModelFile(model);
				if (isGaeaModel(model.getJarName())) {
					// 解析参数
					model.setParams(new ModelMetadata(model.getDiskFile()).getParamsJson());
				}
			}
			aiModelMapper.update(model);

			// 加载模型到classpath
			if (model.getDiskFile() != null && isGaeaModel(model.getJarName())) {
				loadModelJar(model.getDiskFile());
			}
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("编辑AI模型失败: {}", model, e);
			throw new IllegalStateException("编辑失败");
		}
	}

	@Override
	@Transactional
	public Result delete(String uuid) {
		try {
			Integer countNum = referMapper.countBySubjectUuid(uuid);
			if (countNum != null && countNum > 0) {
				return Result.newError().withMsg("ai模型已被使用，无法删除");
			}

			int rows = aiModelMapper.delete(uuid);
			if (rows <= 0) {
				logger.warn("删除模型失败: {}", uuid);
				throw new IllegalStateException("删除模型失败");
			}

			Micro micro = microMapper.selectMicroByPackageUuid(uuid);
			if (micro != null) {
				microService.deleteMicro(micro.getUuid());
			}
			return Result.newSuccess();
		} catch (Exception e) {
			logger.error("删除AI模型失败: {}", uuid, e);
			throw new IllegalStateException("删除失败");
		}
	}

	@Override
	@Transactional
	public void enable(String uuid) {
		try {
			editEnable(uuid, true);
		} catch (Exception e) {
			logger.error("启用AI模型失败: {}", uuid, e);
			throw new IllegalStateException("启用失败");
		}
	}

	@Override
	@Transactional
	public void disable(String uuid) {
		try {
			editEnable(uuid, false);
		} catch (Exception e) {
			logger.error("禁用AI模型失败: {}", uuid, e);
			throw new IllegalStateException("禁用失败");
		}
	}

	@Override
	public Result listAll(int page, int pageNum, AiModel aiModel, boolean isAll, String sortField, String sortDirection) {
		try {
			if (isAll) { // 查询所有模型
				int total = aiModelMapper.countTotal(null, null, UserHolder.hasAdminPermission(), aiModel);
				if (total <= 0) {
					return Result.newSuccess().withData(Collections.emptyList());
				}


				List<AiModel> list = aiModelMapper.selectAll(null, null,
						UserHolder.hasAdminPermission(), aiModel, "update_time", "desc");
				return Result.newSuccess().withData(list).withTotal(total);
			} else { // 分页查询
				int total = aiModelMapper.countTotal(null, pageNum,
						UserHolder.hasAdminPermission(), aiModel);
				if (total <= 0) {
					return Result.newSuccess().withData(Collections.emptyList());
				}

				List<AiModel> list = aiModelMapper.selectAll(
						(page - 1) * pageNum, pageNum,
						UserHolder.hasAdminPermission(), aiModel, sortField, sortDirection
				);
				return Result.newSuccess().withData(list).withTotal(total);
			}
		} catch (Exception e) {
			logger.error("查询AI模型列表失败. model: {}", aiModel, e);
			return Result.newSuccess().withData(Collections.emptyList());
		}
	}

	@Override
	public JSONArray predictByModelName(String json) {
		try {
			JSONObject jsonObject = JSON.parseObject(json);
			String modelName = jsonObject.getString("model");
			AiModel aiModel = aiModelMapper.selectByModelName(modelName);
			if (aiModel == null) {
				throw new IllegalArgumentException("模型不存在");
			}
			if (StringUtils.isBlank(aiModel.getJarName())) {
				throw new IllegalArgumentException("模型文件名称不存在");
			}
			jsonObject.put("model", aiModel.getJarName());
			return predictByJarName(jsonObject);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	@Override
	public JSONArray predictByJarName(JSONObject jsonObject) {
		String jarName = jsonObject.getString("model");
		try {
			JSONArray dataArray = jsonObject.getJSONArray("data");
			if (dataArray == null) {
				logger.info("模型预测参数data为空");
				return null;
			}
			Invoker invoker;
			if (jarName.endsWith(".jar")) {
				invoker = JarInvoker.newInstance(new File(resourceResolver.getModelPath(), jarName));
			} else if (jarName.endsWith(".pmml") || jarName.endsWith(".xml")) {
				invoker = PmmlInvoker.newInstance(new File(resourceResolver.getModelPath(), jarName));
			} else {
				throw new IllegalArgumentException("请检查模型名称");
			}

			JSONArray results = new JSONArray(dataArray.size());
			for (int i = 0; i < dataArray.size(); i++) {
				JSONObject obj = dataArray.getJSONObject(i);
				String objResult = invoker.invoke(obj);
				if (StringUtils.isNotBlank(objResult)) {
					results.add(JSON.parseObject(objResult));
				}
			}
			return results;
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	private void editEnable(String uuid, boolean enable) {
		AiModel model = new AiModel();
		model.setUuid(uuid);
		model.setEnable(enable);
		aiModelMapper.updateEnable(model);
		initModelAction();
	}

	/**
	 * 把模型jar包载入classpath
	 * @param modelJar 模型jar包
	 */
	private void loadModelJar(File modelJar) throws IOException, InvocationTargetException, IllegalAccessException {
		if (!isGaeaModel(modelJar.getName())) {
			return;
		}
		if (modelJar.getName().endsWith(".jar")) {
			addURL.invoke(classLoader, modelJar.toURI().toURL());
		} else {
		    String jarName = modelJar.getName();
			FileUtils.copyFile(
					modelJar,
					SafelyFiles.newFile(System.getProperty("gaea.tmpdir"), jarName)
			);
		}
	}

	@Override
	public Result downModel(String uuid, HttpServletResponse response) {
		if (StringUtils.isBlank(uuid)) {
			throw new IllegalArgumentException("参数不能为空");
		}
		try {
			String jarName = aiModelMapper.selectJarNameByUuid(uuid);
			if (StringUtils.isBlank(jarName)) {
				return Result.newError().withMsg("找不到该模型");
			}
			String realPath = resourceResolver.getModelPath() + File.separator + jarName;
			File file = SafelyFiles.newFile(realPath);
			if (!file.exists()) {
			    logger.warn("机器学习模型文件不存在: {}", file);
				throw new IllegalArgumentException("模型" + jarName + "不存在");
			}
			response.setCharacterEncoding("utf-8");
			response.setContentType("multipart/form-data");
			response.setHeader("Content-Disposition", "attachment;fileName=" + jarName);
			InputStream inputStream = new FileInputStream(file);
			OutputStream os = response.getOutputStream();
			byte[] b = new byte[2048];
			int length;
			while ((length = inputStream.read(b)) > 0) {
				os.write(b, 0, length);
			}
			// 这里主要关闭。
            os.flush();
			os.close();
			inputStream.close();
			return Result.newSuccess();
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
            logger.error("下载机器学习模型文件失败. uuid: {}", uuid, e);
		}
		return Result.newError();
	}

	@Override
	@Transactional
	public Result apiPublish(MultipartFile file, String params) {
		if (file == null || StringUtils.isBlank(file.getOriginalFilename())) {
			throw new IllegalArgumentException("模型文件不能为空");
		}

		AiModel model = new AiModel();
		model.setFormFile(file);
		validateModelJarName(model);

		try {
			// 保存文件流到磁盘上
			saveModelFile(model);
			if (isGaeaModel(model.getJarName())) {
				// 解析参数
				model.setParams(new ModelMetadata(model.getDiskFile()).getParamsJson());
				model.setEnable(true);
			} else {
				model.setEnable(false);
			}

			List<AiModel> dbModels = aiModelMapper.selectByModelNameOrJarName(null, model.getJarName());
			if (!dbModels.isEmpty()) { // 模型已经存在
				model.setUuid(dbModels.get(0).getUuid());
				aiModelMapper.update(model);
				if (isGaeaModel(model.getJarName())) {
					loadModelJar(model.getDiskFile());
					createModelMicro(model);
				}
				return Result.newSuccess().withCode(BizCode.AIMODEL_ISEXIST);
			}

			model.setModelName(model.getJarName());
			model.setUuid(IdUtils.UUID());
			aiModelMapper.insert(model);
			loadModelJar(model.getDiskFile());
			if (isGaeaModel(model.getJarName())) {
				createModelMicro(model);
			}
			return Result.newSuccess();
		} catch (Exception e) {
			logger.error(model + ", " + e.getLocalizedMessage(), e);
			return Result.newError().withMsg("模型发布失败");
		}
	}

	@Override
	public Result microForModel(String modelUuid) {
		if (StringUtils.isBlank(modelUuid)) {
			throw new IllegalArgumentException("参数不能为空");
		}

		try {
			return Result.newSuccess().withData(microMapper.selectByModel(modelUuid));
		} catch (Exception e) {
			logger.error(modelUuid + ", " + e.getLocalizedMessage(), e);
			throw e;
		}
	}

	@Override
	public String path() {
		String path = resourceResolver.getModelPath();
		return path;
	}

	@Override
	public Result downModelParams(String uuid, HttpServletResponse response) {
		if (StringUtils.isBlank(uuid)) {
			throw new IllegalArgumentException("参数不能为空");
		}
		AiModel aiModel = aiModelMapper.selectModelByUuid(uuid);
		if (null == aiModel || StringUtil.isBlank(aiModel.getJarName())) {
			logger.error("下载模型参数csv，数据库查询的模型为空, uuid = " + uuid);
			throw new IllegalArgumentException("数据库查询的模型信息为空");
		}

		if (!isGaeaModel(aiModel.getJarName())) {
			logger.error("非gaea平台模型，暂不支持下载参数");
			return Result.newError().withMsg("该模型不支持下载参数");
		}

		try {
			if (StringUtil.isBlank(aiModel.getParams())) {
				String params = ModelUtils.getParamsJsonFromModelFile(new File(resourceResolver.getModelPath(), aiModel.getJarName()));
				if (StringUtil.isBlank(params)) {
					logger.error("Ai模型文件解析参数为空, jarName = " + aiModel.getJarName());
					throw new IllegalArgumentException("模型参数为空");
				}
				aiModel.setParams(params);
			}
			List<String> head = new ArrayList<>();
			//添加title
			head.add("英文名称");
			head.add("中文名称");
			head.add("数据类型");
			head.add("子类型");
			head.add("传递方向");
			head.add("是否必输");
			String params = aiModel.getParams();
			//保持参数顺序一致
			LinkedHashMap<String, Object> json = JSON.parseObject(params, LinkedHashMap.class, Feature.OrderedField);
			JSONObject paramsJson = new JSONObject(true);
			paramsJson.putAll(json);
			List<List<String>> list = new ArrayList<>(paramsJson.keySet().size() + 1);
			list.add(head);
			for (String key : paramsJson.keySet()) {
				String type = paramsJson.getString(key);
				List<String> line = new ArrayList<>(6);
				line.add(key);	//英文名称
				line.add(key);	//中文名称, 模型参数没有中文,所以此项默认为英文
				if ("string".equalsIgnoreCase(type) || type.endsWith("String")){    //添加类型
					line.add("String");
				}else if ("int".equalsIgnoreCase(type) || type.endsWith("Integer")) {
					line.add("Integer");
				} else if ("double".equalsIgnoreCase(type) || type.endsWith("Double")) {
					line.add("Double");
				} else if ("date".equalsIgnoreCase(type) || type.endsWith("Date")) {
					line.add("Date");
				} else if ("long".equalsIgnoreCase(type) || type.endsWith("Long")) {
					line.add("Long");
				} else if ("boolean".equalsIgnoreCase(type) || type.endsWith("Boolean")) {
					line.add("Boolean");
				} else {
					line.add("String");   //默认String
				}
				line.add(" ");	//子类型都为空
				line.add("传入传出");//传递方向默认传入传出
				line.add("是");//默认是必输项
				list.add(line);
			}
			PoiUtil.downExcel(list, false, response);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(),e);
			throw new IllegalArgumentException("下载失败");
		}
		return Result.newSuccess();
	}

	private void validateModel(AiModel model) {
		if (StringUtils.isBlank(model.getUuid()) && model.getFormFile() == null) {
			logger.warn("模型文件流为空: {}", model);
			throw new IllegalArgumentException("请上传模型文件");
		}
		if (StringUtils.isBlank(model.getModelName()) || model.getModelName().length() > 100) {
			logger.warn("模型名称无效: {}", model);
			throw new IllegalArgumentException("模型名称不能为空并且长度不能超过100个字符");
		}
		validateModelJarName(model);
	}

	/**
	 * 根据jarName判断是否为Gaea使用的ai模型，false:客户自定义的模型，只需要上传到gaea,不需要规则中使用
	 * @param jarName
	 * @return
	 */
	private boolean isGaeaModel(String jarName) {
		if (StringUtils.isBlank(jarName)) {
			return false;
		}
		return jarName.endsWith(".jar") || jarName.endsWith(".pmml");
	}

	private void validateModelJarName(AiModel model) {
		if (model.getFormFile() != null) {
			String jarName = model.getFormFile().getOriginalFilename();
			if (!jarName.endsWith(".pmml")) {
				logger.warn("不支持的模型文件类型: {}", model);
				throw new IllegalArgumentException("仅支持类型为pmml的模型");
			}
			if (jarName.length() > 100) {
				// 模型文件名称超长，截断保留100位
				String ext = jarName.substring(jarName.lastIndexOf('.'));
				jarName = jarName.substring(0, 100 - ext.length()) + ext;
			}
			model.setJarName(jarName);
		}
	}

	private void saveModelFile(AiModel model) throws IOException {
		File diskFile = new File(resourceResolver.getModelPath(), model.getJarName());
		model.getFormFile().transferTo(diskFile);
		model.setDiskFile(diskFile);
	}

	private void writeConf(MicroDeployment microDeployment, String microUuid, Map<String, byte[]> resource) throws IOException {
		JSONObject conf = new JSONObject();
		conf.put("id", microUuid);
		conf.put("deployTime", System.currentTimeMillis());
		conf.put("microType", RestConstants.MicroType.MODEL);

		JSONObject deployment = new JSONObject();
		deployment.put("type", MicroDeployment.DeployType.COMMON); //类别
		JSONArray models = new JSONArray();
		if (microDeployment != null) {
			deployment.put("id", microDeployment.getUuid());
			for (MicroDeployment.Model model : microDeployment.getModels()) {
				JSONObject object = new JSONObject();
				object.put("id", model.getPkgBaseline().getPackageUuid() + "_" + model.getPkgBaseline().getVersionNo());
				object.put("percent", model.getPercent());
				object.put("primary", model.isPrimary());
				models.add(object);
			}
		}
		deployment.put("models", models); //截止时间
		conf.put("deployment", deployment);
		Micro micro = microMapper.selectByUuid(microUuid);
		JSONObject approvalLabel = null;
		if (micro != null) {
		    approvalLabel = JSONObject.parseObject(micro.getApprovalLabel());
		}
		conf.put("approvalLabel", approvalLabel);
        KnowledgePackage knowledgePackage = null;
        if (microDeployment !=null && !org.apache.commons.collections.CollectionUtils.isEmpty(microDeployment.getModels()) &&
            microDeployment.getModels().get(0).getPkgBaseline() != null) {
            knowledgePackage = knowledgePackageMapper.selectByUuid(microDeployment.getModels().get(0).getPkgBaseline().getPackageUuid());
        }
        if (knowledgePackage != null && knowledgePackage.getCreator() != null) {
            JSONObject owner = new JSONObject();
            User creator = knowledgePackage.getCreator();
            owner.put("userUuid", creator.getUuid());
            owner.put("uersName", creator.getUsername());
            User orgUser = userMapper.selectOrgByUuid(creator.getUuid());
            if (orgUser != null && orgUser.getOrg() != null) {
                owner.put("orgUuid", orgUser.getOrg().getUuid());
                owner.put("orgName", orgUser.getOrg().getName());
            }
            conf.put("owner", owner);
        }
		resource.put("default.conf", conf.toJSONString().getBytes());
	}

	private void createModelMicro(AiModel model) throws IOException {
		Micro micro = microMapper.selectMicroByPackageUuid(model.getUuid());
		if (micro == null) {
			micro = new Micro();
			micro.setUuid(IdUtils.UUID());
			micro.setName(model.getModelName());
			micro.setPackageUuid(model.getUuid());
			microMapper.insert(micro);
		}

		//保存发布记录
		MicroDeployment deploy = new MicroDeployment();
		deploy.setType(MicroDeployment.DeployType.COMMON);
		deploy.setUuid(IdUtils.UUID());
		deploy.setMicroUuid(micro.getUuid());
		List<MicroDeployment.Model> models = new ArrayList<>();
		MicroDeployment.Model deployModel = new MicroDeployment.Model();
		deployModel.setUuid(model.getUuid());
		deployModel.setPrimary(true);
		KnowledgePackageBaseline baseline = new KnowledgePackageBaseline();
		baseline.setPackageUuid(model.getUuid());
		baseline.setVersionNo(0);
		deployModel.setPkgBaseline(baseline);
		models.add(deployModel);
		deploy.setModels(models);

		microDeploymentMapper.insertOrUpdate(deploy);
		microDeploymentMapper.insertModel(deploy);

		// 保存服务模型关联
		microRelationMapper.batchInsertOrUpdateModel(Arrays.asList(new MicroModel(micro.getUuid(), model.getUuid())));

		//获取执行服务集群节点
		getAllAvailableClients(micro);

		Result deployResult = deployRule2Clients(model.getDiskFile(), deploy, micro, model.getUuid());
		if (deployResult.getCode() == Result.CODE_ERROR) {
			throw new IllegalStateException("服务在全部集群节点部署失败！");
		}
	}


	private Result deployRule2Clients(File modelFile, MicroDeployment microDeployment, Micro micro, String modelUuid) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Map<String, byte[]> resource = new HashMap<>();
		byte[] microBytes;
		try {
			String ext = modelFile.getName().substring(modelFile.getName().lastIndexOf('.'));
			resource.put(modelUuid + "_" + "0" + ext, FileUtils.readFileToByteArray(modelFile));
			writeConf(microDeployment, micro.getUuid(), resource);
			ZipUtils.compression(baos, resource);
			microBytes = PackageUtils.encrypt(baos.toByteArray());
		} catch (IOException e) {
			logger.error("服务打包失败");
			logger.error(e.getLocalizedMessage(), e);
			throw new IllegalArgumentException("服务打包失败");
		}
		//保存bytes到本地
		try {
			File file = SafelyFiles.newFile(resourceResolver.getDeployPath(),  micro.getUuid() + ".zip");
			FileUtils.writeByteArrayToFile(file, microBytes);
		} catch (Exception e) {
			logger.warn("保存服务到磁盘失败");
			logger.warn(e.getLocalizedMessage(), e);
		}

		Map<String, Boolean> deployResultMap = new HashMap<>(micro.getClients().size());
		List<String> deployFailClients = new ArrayList<>();
		boolean deploySucess = false;
		// 把知识包推到每个集群节点
		for (Client client : micro.getClients()) {
			//TODO 处理集群没有全部替换新知识包的情况
			try {
				clientService.deployRule(client.getBaseUrl(), micro.getUuid(), microBytes);
				deployResultMap.put(client.getBaseUrl(), true);
				//上线成功一个则保存一个集群节点和服务的关联记录
				microRouteService.saveMicroRoute(client.getUuid(), micro.getUuid());
				deploySucess = true;
			} catch (Exception e) {
				deployResultMap.put(client.getBaseUrl(), false);
				deployFailClients.add(client.getUuid());
				logger.warn("模型优化发布知识包到集群节点失败: {}", client, e);
			}
		}
		if (deploySucess) {
		    for (String clientUuid : deployFailClients) {
		        microRouteService.deleteMicroRoute(clientUuid, micro.getUuid());
            }
			return Result.newSuccess().withData(deployResultMap);
		}
		return Result.newError();
	}

	private void getAllAvailableClients(Micro micro) {
		List<Client> clients = null;
		if (!CollectionUtils.isEmpty(micro.getClients())) {
			clients = clientMapper.selectByUuids(micro.getClients());
		}
		clients = clientMapper.selectAll();
		if (clients.isEmpty()) {
			logger.warn("获取集群节点列表失败");
			throw new IllegalStateException("集群节点无效");
		}

		micro.setClients(clients);
	}
}
