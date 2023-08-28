package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.ruleengine.util.PackageUtils;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.entity.*;
import com.beagledata.gaea.workbench.mapper.AiModelMapper;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.mapper.AssetsTemplateMapper;
import com.beagledata.gaea.workbench.mapper.FolderMapper;
import com.beagledata.gaea.workbench.mapper.FunctionDefinitionMapper;
import com.beagledata.gaea.workbench.mapper.KnowledgePackageBaselineMapper;
import com.beagledata.gaea.workbench.mapper.KnowledgePackageMapper;
import com.beagledata.gaea.workbench.mapper.ProjectMapper;
import com.beagledata.gaea.workbench.mapper.ProjectUserMapper;
import com.beagledata.gaea.workbench.mapper.ReferMapper;
import com.beagledata.gaea.workbench.rule.define.Constant;
import com.beagledata.gaea.workbench.service.AssetsService;
import com.beagledata.gaea.workbench.service.ProjectService;
import com.beagledata.gaea.workbench.service.UserService;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by liulu on 2017/12/29.
 */
@Service
public class ProjectServiceImpl implements ProjectService {
	private static Logger logger = LogManager.getLogger(ProjectServiceImpl.class);

	@Autowired
	private ProjectMapper projectMapper;
	@Autowired
	private FolderMapper folderMapper;
	@Autowired
	private AssetsMapper assetsMapper;
	@Autowired
	private KnowledgePackageMapper knowledgePackageMapper;
	@Autowired
	private ResourceResolver resourceResolver;
	@Autowired
	private ProjectUserMapper projectUserMapper;
	@Autowired
	private AiModelMapper aiModelMapper;
	@Autowired
	private AssetsTemplateMapper assetsTemplateMapper;
	@Autowired
	private KnowledgePackageBaselineMapper knowledgePackageBaselineMapper;
	@Autowired
	private AssetsService assetsService;
	@Autowired
	private ReferMapper referMapper;
	@Autowired
	private FunctionDefinitionMapper functionDefinitionMapper;
	@Autowired
	private UserService userService;
	@Value("${dqc.project-uuid}")
	private String dqcPrjectUuid;

	private Pattern projectNamePattern = Pattern.compile(".*(\\d+)");

	/**
	 * @Author: mahongfei
	 * @description: 添加项目
	 */
	@Override
	@Transactional
	public Map<String, String> addProject(Project project) {
		if (StringUtils.isBlank(project.getName()) || project.getName().length() > 20) {
			throw new IllegalArgumentException("项目名称不能为空并且长度不能超过20个字符");
		}
		if (project.getDescription() != null && project.getDescription().length() > 100) {
			throw new IllegalArgumentException("项目描述不能超过100个字符");
		}

		Map<String, String> map = new HashMap<>(1);
		try {
			project.setUuid(IdUtils.UUID());
			project.setCreator(UserHolder.currentUser());
			projectMapper.insert(project);
			ProjectUser projectUser = new ProjectUser();
			projectUser.setProjectUuid(project.getUuid());
			projectUser.setUserUuid(UserHolder.currentUserUuid());
			projectUserMapper.insert(projectUser);
			map.put("uuid", project.getUuid());
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("项目名称不能重复");
		} catch (Exception e) {
			logger.error("添加项目失败: {}", project, e);
			throw new IllegalStateException("添加失败");
		}
		return map;
	}

	/**
	 * @Author: mahongfei
	 * @description: 删除项目
	 */
	@Override
	@Transactional
	public Result deleteProject(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			throw new IllegalArgumentException("uuid不能为空");
		}

		try {
			int count = projectMapper.countEffectiveService(uuid);
			if (count > 0) {
				return Result.newError().withMsg("项目有已生效的服务，无法删除");
			}
			projectMapper.delete(new Project(uuid));
			projectMapper.deleteProjectService(uuid);
			return Result.SUCCESS;
		} catch (Exception e) {
			logger.error("删除服务失败: {}", uuid, e);
			throw new IllegalStateException("删除失败");
		}
	}

	/**
	 * @Author: mahongfei
	 * @description: 编辑项目
	 */
	@Override
	public void editProject(Project project) {
		if (StringUtils.isBlank(project.getUuid())) {
			throw new IllegalArgumentException("uuid不能为空");
		}
		if (StringUtils.isBlank(project.getName()) || project.getName().length() > 20) {
			throw new IllegalArgumentException("项目名称不能为空并且长度不能超过20个字符");
		}
		if (project.getDescription() != null && project.getDescription().length() > 100) {
			throw new IllegalArgumentException("项目描述长度不能超过100个字符");
		}

		try {
			projectMapper.update(project);
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("项目名称不能重复");
		} catch (Exception e) {
			logger.error("编辑项目失败: {}", project, e);
			throw new IllegalArgumentException("编辑失败");
		}
	}

	/**
	 * @Author: mahongfei
	 * @description: 项目列表
	 */
	@Override
	public Result listPage(int page, int pageNum, String name, String sortField, String sortDirection) {
		try {
			boolean isAdmin = UserHolder.hasAdminPermission();
			boolean isOrg = UserHolder.isOrgAdmin();
			int total = projectMapper.count(name, UserHolder.currentUserUuid(), isAdmin, isOrg);
			if (total <= 0) {
				return Result.newSuccess().withTotal(total).withData(Collections.emptyList());
			}

			List<Project> list = projectMapper.selectPage((page - 1) * pageNum, pageNum, name,
					UserHolder.currentUserUuid(), isAdmin, sortField, sortDirection, isOrg);
			for (Project project : list) {
				if (isAdmin || (project.getCreator() != null && UserHolder.currentUser().getUsername().equals(project.getCreator().getUsername()))) {
					// 用户是管理员或者是项目创建者，拥有编辑项目的权限
					project.setUserEdit(true);
				}
			}

			return Result.newSuccess().withTotal(total).withData(list);
		} catch (Exception e) {
			logger.error("查询项目列表失败. page: {}, pageNum: {}, name: {}, sortField: {}, sortDirection: {}", page, pageNum, name, sortField, sortDirection, e);
			throw new IllegalArgumentException("项目获取错误");
		}
	}

	/**
	 * @Author: mahongfei
	 * @description: 项目详情
	 */
	@Override
	public Project projectDetails(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			throw new IllegalArgumentException("uuid不能为空");
		}

		try {
			boolean isAdmin = UserHolder.hasAdminPermission();
			boolean isOrg = UserHolder.isOrgAdmin();
			Project project = projectMapper.selectByUuidForUser(uuid, UserHolder.currentUserUuid(), isAdmin, isOrg);
			if (project == null) {
				throw new IllegalArgumentException("项目不存在");
			}
			return project;
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("查询项目详情失败: {}", uuid, e);
			throw new IllegalArgumentException("查询失败");
		}
	}

	@Override
	public void export(String uuid, HttpServletResponse response) {
		Project project = projectMapper.selectByUuid(uuid);
		if (project == null) {
			throw new IllegalArgumentException("项目不存在");
		}

		OutputStream os = null;
		try {
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(project.getName().getBytes(), "ISO-8859-1") + ".zip");
			ByteArrayOutputStream outputStream  = new ByteArrayOutputStream();
			Set<String> uuids = new HashSet<>();
			try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
				zipVersion(zip);
				zipAssetsVersion(uuid, zip);
				zipProject(project, zip);
				zipAssets(uuid, zip, uuids);
				zipFolders(uuid, zip);
				zipPkgs(uuid, zip);
				zipPkgBaseline(uuid, zip);
				zipPkgAssets(uuid, zip);
				ziptAssetsTemplate(uuid, zip, uuids);
				zipModel(uuids, zip);
				zipFunction(uuids, zip);
				zip.closeEntry();
				zip.flush();
				byte[] bytes = PackageUtils.encrypt(outputStream.toByteArray());
				os = response.getOutputStream();
				os.write(bytes);
				os.flush();
			}
		} catch (Exception e) {
			logger.error("导出项目失败: {}", uuid, e);
			throw new IllegalStateException("导出失败");
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	@Override
	public Result listProjectsByUser(Integer page, Integer pageNum, String userUuid, String projectName, String sortField, String sortDirection) {
		if (StringUtils.isBlank(userUuid)) {
			throw new IllegalArgumentException("用户uuid不能为空");
		}

		try {
			int total = projectMapper.selectCountByUserUuid(userUuid, projectName);
			if (total > 0) {
				List<Project> list = projectMapper.selectByUserUuid(pageNum * (page - 1), pageNum, userUuid, projectName, sortField, sortDirection);
				return Result.newSuccess().withData(list).withTotal(total);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), userUuid, projectName, e);
		}
		return Result.emptyList();
	}

	@Override
	public boolean initDqcProject() {
		try {
			User superAdmin = userService.getByUsername(Constants.User.SUPERADMIN_USERNAME);
			if (null == superAdmin) {
				logger.error("未找到关联用户, 请检查数据");
				return false;
			}
			String projectName = "数据质量工具";
			Project project = projectMapper.selectByProjectName(projectName);
			if (null != project && StringUtils.isNotBlank(project.getUuid())) {
				logger.info("已存在关联项目, projectUuid=[{}]", project.getUuid());
				return true;
			}
			project = new Project();
			logger.info("开始新建关联项目");
			if (StringUtils.isBlank(dqcPrjectUuid)) {
				dqcPrjectUuid = IdUtils.UUID();
				logger.info("配置的dqc.project-uuid为空,自动创建新的projectUuid");
			}
			logger.info("使用的projectUuid为: [{}]", dqcPrjectUuid);
			project.setUuid(dqcPrjectUuid);
			project.setCreator(superAdmin);
			project.setName(projectName);
			projectMapper.insert(project);
			ProjectUser projectUser = new ProjectUser();
			projectUser.setProjectUuid(project.getUuid());
			projectUser.setUserUuid(superAdmin.getUuid());
			projectUserMapper.insert(projectUser);
			logger.info("新建数据质量关联项目完成, projectUuid=[{}]", project.getUuid());
			return true;
		} catch (DuplicateKeyException e) {
			logger.error("项目名称不能重复");
			return false;
		} catch (Exception e) {
			logger.error("添加数据质量项目失败: {}", e);
			return false;
		}
	}

	private void zipModel(Set<String> uuids, ZipOutputStream zip) throws IOException {
		if (uuids.isEmpty()) {
			return;
		}

		Set<AiModel> models = referMapper.selectAiModelByReferUuid(uuids);
		if (models.isEmpty()) {
			return;
		}

		Iterator<AiModel> it = models.iterator();
		while (it.hasNext()) {
			AiModel model = it.next();
			File modelFile = SafelyFiles.newFile(resourceResolver.getModelPath(), model.getJarName());
			if (modelFile.exists()) {
				zip.putNextEntry(new ZipEntry(model.getJarName()));
				zip.write(FileUtils.readFileToByteArray(modelFile));
			} else {
				it.remove();
			}
		}

		String modelLines = models.stream().map(m -> JSON.toJSONString(m)).collect(Collectors.joining("\n"));
		zip.putNextEntry(new ZipEntry("models"));
		zip.write(modelLines.getBytes());
	}

	/**
	 * 写一个版本文件，用作导入识别
	 *
	 * @param zip
	 * @throws IOException
	 */
	private void zipVersion(ZipOutputStream zip) throws IOException {
		zip.putNextEntry(new ZipEntry("version"));
		zip.write("1.1".getBytes());
	}

	/**
	 * 项目写入zip流
	 *
	 * @param project
	 * @param zip
	 * @throws IOException
	 */
	private void zipProject(Project project, ZipOutputStream zip) throws IOException {
		zip.putNextEntry(new ZipEntry("project"));
		zip.write(JSON.toJSONBytes(project));
	}

	private void zipFunction(Set<String> uuids, ZipOutputStream zip) throws IOException {
		if (uuids.isEmpty()) {
			return;
		}

		List<FunctionDefinition> funcs = referMapper.selectFunctionByReferUuids(uuids);
		String lines = funcs.stream().map(f -> JSON.toJSONString(f)).collect(Collectors.joining("\n"));
		zip.putNextEntry(new ZipEntry("functionDefinitions"));
		zip.write(lines.getBytes());
	}

	/**
	 * 资源文件写入zip流，解析出依赖的AI模型，把jar写入zip
	 *
	 * @param projectUuid
	 * @param zip
	 * @throws IOException
	 */
	private void zipAssets(String projectUuid, ZipOutputStream zip, Set<String> uuids) throws IOException {
		List<Assets> assetsList = assetsMapper.selectByProject(projectUuid);
		String assetsLines = assetsList.stream().map(assets -> JSON.toJSONString(assets)).collect(Collectors.joining("\n"));
		assetsList.forEach(assets -> uuids.add(assets.getUuid()));
		zip.putNextEntry(new ZipEntry("assets"));
		zip.write(assetsLines.getBytes());
	}

	/**
	 * @Author yangyongqiang
	 * @Description 导出基线版本zip流
	 * @Date 7:31 下午 2020/7/23
	 **/
	private void zipPkgBaseline(String projectUuid, ZipOutputStream zip) throws IOException {
		List<KnowledgePackageBaseline> knowledgePackageBaselines = knowledgePackageBaselineMapper.selectBaselineByPackageUuid(projectUuid);
		String knowledgePackageBaselinesLines = knowledgePackageBaselines.stream().map(knowledgePackageBaseline -> JSON.toJSONString(knowledgePackageBaseline)).collect(Collectors.joining("\n"));
		zip.putNextEntry(new ZipEntry("pkgBaseline"));
		zip.write(knowledgePackageBaselinesLines.getBytes());
	}

	/**
	 * @Author yangyongqiang
	 * @Description 导出知识包资源文件zip流
	 * @Date 7:35 下午 2020/7/23
	 **/
	private void zipPkgAssets(String projectUuid, ZipOutputStream zip) throws IOException {
		List<KnowledgePackage> knowledgePackages = knowledgePackageMapper.selectPkgAssetsByProjectUuid(projectUuid);
		String knowledgePackagesLines = knowledgePackages.stream().map(knowledgePackage -> JSON.toJSONString(knowledgePackage)).collect(Collectors.joining("\n"));
		zip.putNextEntry(new ZipEntry("pkgAssets"));
		zip.write(knowledgePackagesLines.getBytes());
	}

	/**
	 * @Author yangyongqiang
	 * @Description 导出资源文件版本zip流
	 * @Date 6:33 下午 2020/7/23
	 **/
	private void zipAssetsVersion(String projectUuid, ZipOutputStream zip) throws IOException {
		List<AssetsVersion> assetsVersions = assetsMapper.selectVersionsByProjectUuid(projectUuid);
		String assetsVersionLines = assetsVersions.stream().map(assetsVersion -> JSON.toJSONString(assetsVersion)).collect(Collectors.joining("\n"));
		zip.putNextEntry(new ZipEntry("assetsVersions"));
		zip.write(assetsVersionLines.getBytes());
	}

	/**
	 * 文件夹写入zip流
	 *
	 * @param projectUuid
	 * @param zip
	 * @throws IOException
	 */
	private void zipFolders(String projectUuid, ZipOutputStream zip) throws IOException {
		List<Folder> folders = folderMapper.selectByProjectUuid(projectUuid);
		String folderLines = folders.stream().map(folder -> JSON.toJSONString(folder)).collect(Collectors.joining("\n"));
		zip.putNextEntry(new ZipEntry("folders"));
		zip.write(folderLines.getBytes());
	}

	/**
	 * @Author yangyongqiang
	 * @Description 资源文件模版写入zip流
	 * @Date 4:56 下午 2020/7/23
	 **/
	private void ziptAssetsTemplate(String projectUuid, ZipOutputStream zip, Set<String> uuids) throws IOException {
		List<AssetsTemplate> assetsTemplates = assetsTemplateMapper.selectByProjectUuid(projectUuid);
		String assetsTemplateLines = assetsTemplates.stream().map(assetsTemplate -> JSON.toJSONString(assetsTemplate)).collect(Collectors.joining("\n"));
		assetsTemplates.forEach(t -> uuids.add(t.getUuid()));
		zip.putNextEntry(new ZipEntry("assetsTemplate"));
		zip.write(assetsTemplateLines.getBytes());
	}

	/**
	 * 知识包和资源关联写入zip流
	 *
	 * @param projectUuid
	 * @param zip
	 * @throws IOException
	 */
	private void zipPkgs(String projectUuid, ZipOutputStream zip) throws IOException {
		List<KnowledgePackage> pkgs = knowledgePackageMapper.selectAll(projectUuid, false);
		String pkgsLines = pkgs.stream().map(pkg -> JSON.toJSONString(pkg)).collect(Collectors.joining("\n"));
		zip.putNextEntry(new ZipEntry("pkgs"));
		zip.write(pkgsLines.getBytes());
	}

	@Override
	@Transactional
	public Result importProject(byte[] bytes) {
		ZipEntry zipEntry;
		String version = null;
		String projectJson = null;
		List<String> folderLines = null;
		List<String> modelLines = null;
		List<String> assetsLines = null;
		List<String> assetsVersionLines = null;
		List<String> templateLines = null;
		List<String> pkgLines = null;
		List<String> pkgAssetsLines = null;
		List<String> pkgBaselineLines = null;
		List<String> funcLines = null;
		Map<String, byte[]> modelFiles = new HashMap<>();
		ZipInputStream zis = null;
		try {
			byte[] in2b = PackageUtils.decrypt(bytes);
			zis = new ZipInputStream(new ByteArrayInputStream(in2b));
			while ((zipEntry = zis.getNextEntry()) != null) {
				String entryName = zipEntry.getName();
				if ("version".equals(entryName)) {
					version = IOUtils.toString(zis);
				} else if ("project".equals(entryName)) {
					projectJson = IOUtils.toString(zis);
				} else if ("folders".equals(entryName)) {
					folderLines = IOUtils.readLines(zis);
				} else if ("models".equals(entryName)) {
					modelLines = IOUtils.readLines(zis);
				} else if ("assets".equals(entryName)) {
					assetsLines = IOUtils.readLines(zis);
				}  else if ("assetsVersions".equals(entryName)) {
					assetsVersionLines = IOUtils.readLines(zis);
				}   else if ("assetsTemplate".equals(entryName)) {
					templateLines = IOUtils.readLines(zis);
				} else if ("pkgs".equals(entryName)) {
					pkgLines = IOUtils.readLines(zis);
				} else if ("pkgAssets".equals(entryName)) {
					pkgAssetsLines = IOUtils.readLines(zis);
				} else if ("pkgBaseline".equals(entryName)) {
					pkgBaselineLines = IOUtils.readLines(zis);
				} else if ("functionDefinitions".equals(entryName)) {
					funcLines = IOUtils.readLines(zis);
				} else if (entryName.endsWith(".jar") || entryName.endsWith(".xml") || entryName.endsWith(".pmml")) {
					modelFiles.put(entryName, IOUtils.toByteArray(zis));
				}
			}
			if (!"1.1".equals(version)) {
				throw new IllegalArgumentException("当前文件版本低，请下载新的文件再导入");
			}

			Map<String, String> typeMappings = new HashMap<>();
			Project project = doImportProject(projectJson);
			// 批量插入文件夹并返回新旧文件夹uuid映射
			Map<String, String> folderMappings = importFolders(project, folderLines);
			// 批量插入模型文件并返回新旧模型文件uuid映射
			Map<String, String> modelMappings = importModels(modelLines, modelFiles);
			// 插入方法并返回方法名称映射
			Map<String, String> funcNameMap = importFuncs(funcLines);
			// 批量插入资源文件并返回新旧资源文件uuid映射
			// 先拆分变量和规则
			Map<String, String> uuidMappings = new HashMap<>();
			// 变量文件
			List<Assets> factAssets = new ArrayList<>();
			//规则文件
			List<Assets> ruleAssets = new ArrayList<>();
			//拆分变量和规则 并获取新旧uuid映射
			parserAssetsLines(project.getUuid(), assetsLines, uuidMappings, folderMappings, typeMappings, factAssets, ruleAssets);
			// 导入数据模型并获取数据模型的 新旧id映射
			Map<Integer, Integer> factIdMappings = importFact(factAssets, uuidMappings, funcNameMap);
			// 导入数据模型并获取数据模型的 新旧id映射
			importRuleAssets(ruleAssets, folderMappings, modelMappings, uuidMappings, factIdMappings, funcNameMap);
			// 批量插入资源版本文件
			importAssetsVersion(uuidMappings, modelMappings, assetsVersionLines, typeMappings, factIdMappings, funcNameMap);
			// 批量插入模板
			importTemplate(project, templateLines, funcNameMap);
			// 批量插入知识包并返回新旧知识包uuid映射
			Map<String, String> pkgMappings = importPkgs(project, pkgLines);
			// 批量插入知识包基线
			importPkgBaseline(pkgMappings, pkgBaselineLines);
			// 批量插入知识包资源文件
			importPkgAssets(pkgMappings, uuidMappings, pkgAssetsLines);
			return Result.newSuccess().withData(Collections.singletonMap("uuid", project.getUuid()));
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("导入项目失败", e);
			throw new IllegalStateException("导入失败");
		} finally {
			IOUtils.closeQuietly(zis);
		}
	}

	/**
	* 描述: 导入规则文件
	* @param: [ruleAssets, folderMappings, modelMappings, uuidMappings, factIdMappings, funcNameMap]
	* @author: 周庚新
	* @date: 2020/12/16
	* @return: void
	*/
	private void importRuleAssets(List<Assets> ruleAssets, Map<String, String> folderMappings, Map<String, String> modelMappings, Map<String, String> uuidMappings, Map<Integer, Integer> factIdMappings, Map<String, String> funcNameMap) {
		if (CollectionUtils.isEmpty(ruleAssets)) {
			return;
		}
		ruleAssets.forEach(assets -> {
			String content = assets.getContent();
			String type = assets.getType();
			if (StringUtils.isNotBlank(content) && !AssetsType.CONSTANT.equals(type)) {
				if (AssetsType.RULE_SCRIPT.equals(type)) {
					for (Map.Entry<Integer, Integer> entry : factIdMappings.entrySet()) {
					    content = content.replace("Fact_" + entry.getKey(), "Fact_" + entry.getValue());
					}
				} else {
					//替换资源文件uuid
					for (Map.Entry<String, String> entry : uuidMappings.entrySet()) {
						content = content.replace(entry.getKey(), entry.getValue());
					}
					//替换ai模型uuid
					for (Map.Entry<String, String> entry : modelMappings.entrySet()) {
						content = content.replace(entry.getKey(), entry.getValue());
					}
					//替换方法名称
					for (Map.Entry<String, String> entry : funcNameMap.entrySet()) {
						content = content.replace("\"name\":\"" + entry.getKey(), "\"name\":\"" + entry.getValue());
					}
				}
				assets.setContent(content);
			}
		});
		assetsMapper.insertBatch(ruleAssets);

		for (Assets assets : ruleAssets) {
			Set<Refer> refers = assetsService.getRefers(assets);
			if (!refers.isEmpty()) {
				referMapper.insertBatch(refers);
			}
		}
	}

	/**
	 * 描述: 导入数据模型，常量
	 * @param: [factAssets, uuidMappings, fieldMappings, funNameMap]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: java.util.Map<java.lang.Integer, java.lang.Integer>
	 */
	private Map<Integer, Integer> importFact(List<Assets> factAssets, Map<String, String> uuidMappings, Map<String, String> funNameMap) {
		Map<Integer, Integer> idMappings = new HashMap<>();
		if (CollectionUtils.isEmpty(factAssets)) {
			return idMappings;
		}
		factAssets.forEach(assets -> {
			String content = assets.getContent();
			Integer oldId = assets.getId();
			if (StringUtils.isNotBlank(content) && AssetsType.FACT.equals(assets.getType())) {
				//替换资源文件uuid
				for (Map.Entry<String, String> entry : uuidMappings.entrySet()) {
					content = content.replace(entry.getKey(), entry.getValue());
				}
				List<String> deriveDatas = (ArrayList<String>) JSONPath.read(content, "$..deriveData");
				deriveDatas = deriveDatas.stream().filter(deriveData -> StringUtils.isNotBlank(deriveData)).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(deriveDatas)) {
					//替换方法名称
					for (Map.Entry<String, String> entry : funNameMap.entrySet()) {
						content = content.replace("\"name\":\"" + entry.getKey(), "\"name\":\"" + entry.getValue());
					}
				}
				assets.setContent(content);
			}
			assetsMapper.insert(assets);
			idMappings.put(oldId, assets.getId());
		});
		for (Assets assets : factAssets) {
			Set<Refer> refers = assetsService.getRefers(assets);
			if (CollectionUtils.isNotEmpty(refers)) {
			    referMapper.insertBatch(refers);
			}
		}
		return idMappings;
	}

	/**
	 * 解析 资源文件，分为数据模型和规则文件 并映射相关 uuid 类型
	 * @param assetslLines
	 * @param uuidMappings
	 * @param typeMappings
	 * @param factAssets
	 * @param ruleAssets
	 */
	private void parserAssetsLines(String projectUuid, List<String> assetslLines, Map<String, String> uuidMappings,
								   Map<String, String> folderMappings, Map<String, String> typeMappings, List<Assets> factAssets, List<Assets> ruleAssets) {
		assetslLines.forEach(line -> {
			Assets assets = JSON.parseObject(line, Assets.class);

			assets.setProjectUuid(projectUuid);
			assets.setCreator(UserHolder.currentUser());

			String oldUuid = assets.getUuid();
			String newUuid = IdUtils.UUID();
			assets.setUuid(newUuid);
			uuidMappings.put(oldUuid, newUuid);

			String type = assets.getType();
			typeMappings.put(oldUuid, type);

			String dirId = assets.getDirParentId();
			if (!"0".equals(dirId)) {
			    String newFolderUuid = folderMappings.get(dirId);
			    assets.setDirParentId(Optional.ofNullable(newFolderUuid).orElse("0"));
			}

			if (AssetsType.FACT.equals(type)) {
				factAssets.add(assets);
			} else {
				ruleAssets.add(assets);
			}
		});
	}

	private Map<String, String> importFuncs(List<String> funcLines) {
		if (funcLines.isEmpty()) {
			return new HashMap<>();
		}

		Map<String, String> funcNameMap = new HashMap<>();
		for (String line : funcLines) {
			FunctionDefinition func = JSON.parseObject(line, FunctionDefinition.class);
			List<FunctionDefinition> existsFunc = functionDefinitionMapper.selectByNameOrClassName(func.getName(), func.getClassName());
			if (existsFunc.isEmpty()) {
				func.setUuid(IdUtils.UUID());
				func.getCreator().setUuid(UserHolder.currentUserUuid());
				functionDefinitionMapper.insert(func);
			} else {
				String oldName = func.getName();
				String oldClassName = func.getClassName();
				boolean contentNotExists = true;
				String content = func.getContent();
				for (FunctionDefinition fd : existsFunc) {
					if (StringUtils.isNotBlank(content) && content.equals(fd.getContent())){
						contentNotExists = false;
						break;
					}
				}

				if (contentNotExists) {
					String[] oldClassNames = oldClassName.split("\\.");
					oldClassName = oldClassNames[oldClassNames.length - 1];
					copyFunctionName(func);
					String name = func.getName();
					String className = func.getClassName();
					String[] classNames = className.split("\\.");
					className = classNames[classNames.length - 1];
					content = content.replaceAll(
							"@FunctionProperty(\\s+)?\\((\\\\s+)?name(\\s+)?=(\\s+)?\""
									+ oldName
									+ "\"(\\s+)?\\)",
							"@FunctionProperty(name = \"" + name + "\")"
					);
					String regex = "class(\\s+)?" + oldClassName;
					content = content.replaceAll(regex, "class " + className);
					funcNameMap.put(oldName, name);
					func.setUuid(IdUtils.UUID());
					func.getCreator().setUuid(UserHolder.currentUserUuid());
					func.setContent(content);
					functionDefinitionMapper.insert(func);
				}
			}
		}
		return funcNameMap;
	}

	private void copyFunctionName(FunctionDefinition func) {
		List<FunctionDefinition> existsFunc = functionDefinitionMapper.selectByNameOrClassName(func.getName(), func.getClassName());
		if (!existsFunc.isEmpty()) {
			String name = func.getName();
			String className = func.getClassName();
			Matcher matcher = projectNamePattern.matcher(name);
			if (matcher.find()) {
				String indexStr = matcher.group(1);
				int length = indexStr.length();
				String oldName = name.substring(0, name.length() - length);
				int oldLength = oldName.length();
				int index = NumberUtils.toInt(indexStr) + 1;
				name = oldName + index;
				while (name.length() > 20) {
					name = oldName.substring(0, oldLength - 1) + index;
				}
			} else {
				if (name.length() == 20) {
					name = name.substring(0, 19) + "2";
				} else {
					name = name + "2";
				}
			}

			matcher = projectNamePattern.matcher(className);
			if (matcher.find()) {
				String indexStr = matcher.group(1);
				int length = indexStr.length();
				String oldName = name.substring(0, className.length() - length);
				int oldLength = oldName.length();
				int index = NumberUtils.toInt(indexStr) + 1;
				className = oldName + index;
				while (className.length() > 200) {
					className = oldName.substring(0, oldLength - 1) + index;
				}
			} else {
				if (className.length() == 200) {
					className = className.substring(0, 199) + "2";
				} else {
					className = className + "2";
				}
			}

			func.setName(name);
			func.setClassName(className);
			copyFunctionName(func);
		}
	}

	/**
	* 描述: 批量插入知识包基线关联资源文件
	* @param: [pkgMappings, assetsMappings, pkgAssetsLines]
	* @author: 周庚新
	* @date: 2020/7/23
	* @return: void
	*
	*/
	private void importPkgAssets(Map<String, String> pkgMappings, Map<String, String> assetsMappings, List<String> pkgAssetsLines) {
		if (CollectionUtils.isEmpty(pkgAssetsLines)) {
			return ;
		}
		List<KnowledgePackage> knowledgePackageAssets = pkgAssetsLines.stream()
				.map(line -> {
					KnowledgePackage packageAssets = JSON.parseObject(line, KnowledgePackage.class);
					packageAssets.setAssetsUuid(assetsMappings.get(packageAssets.getAssetsUuid()));
					packageAssets.setPackageUuid(pkgMappings.get(packageAssets.getPackageUuid()));
					return packageAssets;
				}).collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(knowledgePackageAssets)) {
			for (KnowledgePackage pkg : knowledgePackageAssets) {
				Refer refer = new Refer(
						pkg.getAssetsUuid(),
						null,
						null,
						pkg.getAssetsVersion(),
						pkg.getProjectUuid(),
						"pkg",
						pkg.getBaselineVersion()
				);
				referMapper.insert(refer);
			}
			knowledgePackageBaselineMapper.insertBaselineAssetsBatch(knowledgePackageAssets);
		}
	}

	/**
	* 描述: 批量插入知识包基线
	* @param: [pkgMappings, pkgBaselineLines]
	* @author: 周庚新
	* @date: 2020/7/23
	* @return: void
	*
	*/
	private void importPkgBaseline(Map<String, String> pkgMappings, List<String> pkgBaselineLines) {
		if (CollectionUtils.isEmpty(pkgBaselineLines)) {
			return ;
		}
		List<KnowledgePackageBaseline> knowledgePackageBaselines = pkgBaselineLines.stream()
				.map(line -> {
					KnowledgePackageBaseline knowledgePackageBaseline = JSON.parseObject(line, KnowledgePackageBaseline.class);
					String newUuid = IdUtils.UUID();
					knowledgePackageBaseline.setUuid(newUuid);
					knowledgePackageBaseline.setPackageUuid(pkgMappings.get(knowledgePackageBaseline.getPackageUuid()));
					knowledgePackageBaseline.setCreator(UserHolder.currentUser());
					return knowledgePackageBaseline;
				}).collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(knowledgePackageBaselines)) {
			knowledgePackageBaselineMapper.insertBatch(knowledgePackageBaselines);
		}
	}

	/**
	* 描述: 导入模板
	* @param: [project, templateLines]
	* @author: 周庚新
	* @date: 2020/7/23
	* @return: void
	*
	*/
	private void importTemplate(Project project, List<String> templateLines, Map<String, String> funcNameMap) {
		if (CollectionUtils.isEmpty(templateLines)) {
			return ;
		}
		List<AssetsTemplate> assetsTemplates = templateLines.stream()
				.map(line -> {
					AssetsTemplate assetsTemplate = JSON.parseObject(line, AssetsTemplate.class);
					String newUuid = IdUtils.UUID();
					assetsTemplate.setUuid(newUuid);
					assetsTemplate.setProjectUuid(project.getUuid());
					assetsTemplate.setCreator(UserHolder.currentUser());
					String content = assetsTemplate.getContent();
					if (StringUtils.isNotBlank(content)) {
						for (Map.Entry<String, String> entry : funcNameMap.entrySet()) {
							// 替换资源引用方法名称
							content = content.replace("\"name\":\"" + entry.getKey(), "\"name\":\"" + entry.getValue());
						}
 					}
					assetsTemplate.setContent(content);
					return assetsTemplate;
				}).collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(assetsTemplates)) {
			assetsTemplateMapper.insertBatch(assetsTemplates);
		}
	}

	/**
	* 描述: 将版本文件导入
	* @param: [assetsMappings, modelMappings, assetsVersionLines]
	* @author: 周庚新
	* @date: 2020/7/23
	* @return: void
	*
	*/
	private void importAssetsVersion(
			Map<String, String> assetsMappings,
			Map<String, String> modelMappings,
			List<String> assetsVersionLines,
			Map<String, String> typeMappings,
			Map<Integer, Integer> factIdMappings, Map<String, String> funcNameMap) {
		if (CollectionUtils.isEmpty(assetsVersionLines)) {
		    return;
		}
		List<AssetsVersion> assetsList = assetsVersionLines.stream()
				.map(line -> {
					AssetsVersion assets = JSON.parseObject(line, AssetsVersion.class);
					String uuid = assets.getAssetUuid();
					assets.setAssetUuid(assetsMappings.get(uuid));
					assets.setCreator(UserHolder.currentUser());
					assets.setType(typeMappings.get(uuid));
					String content = assets.getContent();
					if (StringUtils.isNotBlank(content)) {
						if (AssetsType.RULE_SCRIPT.equals(typeMappings.get(uuid))) {
							for (Map.Entry<Integer, Integer> entry : factIdMappings.entrySet()) {
								content = content.replace("Fact_" + entry.getKey(), "Fact_" + entry.getValue());
							}
						}
						for (Map.Entry<String, String> entry : funcNameMap.entrySet()) {
							// 替换资源引用方法名称
							content = content.replace("\"name\":\"" + entry.getKey(), "\"name\":\"" + entry.getValue());
						}
						for (Map.Entry<String, String> entry : assetsMappings.entrySet()) { // 替换新的资源文件uuid
							content = content.replace(entry.getKey(), entry.getValue());
						}
						for (Map.Entry<String, String> entry : modelMappings.entrySet()) { // 替换新的AI模型uuid
							content = content.replace(entry.getKey(), entry.getValue());
						}
 					}
					assets.setContent(content);
					return assets;
				}).collect(Collectors.toList());

		if (!assetsList.isEmpty()) {
			List<List<AssetsVersion>> parts = Lists.partition(assetsList, 10);
			parts.forEach(part -> assetsMapper.insertVersionBatch(part));
			for (AssetsVersion assetsVersion : assetsList) {
				Assets assets = new Assets();
				assets.setContent(assetsVersion.getContent());
				assets.setVersionNo(assetsVersion.getVersionNo());
				assets.setType(assetsVersion.getType());
				assets.setUuid(assetsVersion.getAssetUuid());
				Set<Refer> refers = assetsService.getRefers(assets);
				if (!refers.isEmpty()) {
					referMapper.insertBatch(refers);
				}
			}
		}
	}

	/**
	 * 导入项目，重名后缀 - 副本
	 *
	 * @param projectJson
	 * @return
	 */
	private Project doImportProject(String projectJson) {
		Project importProject = JSON.parseObject(projectJson, Project.class);
		Project project = new Project();
		project.setUuid(IdUtils.UUID());
		project.setName(importProject.getName());
		project.setDescription(importProject.getDescription());
		project.setCreator(UserHolder.currentUser());
		forceInsert(project);
		return project;
	}

	private void forceInsert(Project project) {
		try {
			projectMapper.insert(project);
			ProjectUser projectUser = new ProjectUser();
			projectUser.setProjectUuid(project.getUuid());
			projectUser.setUserUuid(UserHolder.currentUserUuid());
			projectUserMapper.insert(projectUser);
		} catch (DuplicateKeyException e) {
			String projectName = project.getName();
			Matcher matcher = projectNamePattern.matcher(projectName);
			if (matcher.find()) {
				String indexStr = matcher.group(1);
				int length = indexStr.length();
				String oldName = projectName.substring(0, projectName.length() - length);
				int oldLength = oldName.length();
				int index = NumberUtils.toInt(indexStr) + 1;
				projectName = oldName + index;
				while (projectName.length() > 20) {
					projectName = oldName.substring(0, oldLength - 1) + index;
				}
			} else {
				if (projectName.length() == 20) {
					projectName = projectName.substring(0, 19) + "2";
				} else {
					projectName = projectName + "2";
				}
			}
			project.setName(projectName);
			forceInsert(project);
		}
	}

	/**
	 * 导入文件夹，替换新的父文件夹uuid
	 *
	 * @param project
	 * @param lines
	 * @return
	 */
	private Map<String, String> importFolders(Project project, List<String> lines) {
		if (lines == null || lines.isEmpty()) {
			return new HashMap<>();
		}

		Map<String, String> uuidMappings = new HashMap<>(lines.size());
		List<Folder> folders = lines.stream()
				.map(line -> {
					Folder folder = JSON.parseObject(line, Folder.class);
					String newUuid = IdUtils.UUID();
					uuidMappings.put(folder.getUuid(), newUuid);
					folder.setUuid(newUuid);
					folder.setProjectUuid(project.getUuid());
					folder.setCreator(UserHolder.currentUser());
					return folder;
				}).collect(Collectors.toList());

		if (!folders.isEmpty()) {
			folders.forEach(folder -> {
				if (!"0".equals(folder.getParentId())) { // 替换新的父文件夹uuid
					String newParentUuid = uuidMappings.get(folder.getParentId());
					folder.setParentId(Optional.ofNullable(newParentUuid).orElse("0"));
				}
			});
			folderMapper.insertBatch(folders);
		}
		return uuidMappings;
	}

	/**
	 * 导入AI模型，先用jar名称去库里查，如果已经有了就不创建
	 *
	 * @param lines
	 * @param modelFiles 模型jar的字节数组
	 * @return
	 * @throws IOException
	 */
	private Map<String, String> importModels(List<String> lines, Map<String, byte[]> modelFiles) throws IOException {
		if (lines == null || lines.isEmpty()) {
			return new HashMap<>();
		}

		Map<String, String> uuidMappings = new HashMap<>(lines.size());
		for (String line : lines) {
			AiModel model = JSON.parseObject(line, AiModel.class);
			List<AiModel> existsModels = aiModelMapper.selectByModelNameOrJarName(model.getModelName(), model.getJarName());
			if (!existsModels.isEmpty()) {
				uuidMappings.put(model.getUuid(), existsModels.get(0).getUuid());
			} else {
				byte[] bytes = modelFiles.get(model.getJarName());
				if (bytes == null) {
					continue;
				}
				File modelFile = new File(resourceResolver.getModelPath(), model.getJarName());
				FileUtils.writeByteArrayToFile(modelFile, bytes);
				String newUuid = IdUtils.UUID();
				uuidMappings.put(model.getUuid(), newUuid);
				model.setUuid(newUuid);
				model.setEnable(true);
				aiModelMapper.insert(model);
			}
		}
		return uuidMappings;
	}

	/**
	* 描述: 导入知识包 并返回知识包新旧uuid映射
	* @param: [project, lines]
	* @author: 周庚新
	* @date: 2020/7/23
	* @return: java.util.Map<java.lang.String,java.lang.String>
	*
	*/
	private Map<String, String> importPkgs(Project project, List<String> lines) {
		Map<String, String> pkgMappings = new HashMap<>();
		if (lines == null || lines.isEmpty()) {
			return pkgMappings;
		}
		List<KnowledgePackage> pkgs = lines.stream()
				.map(line -> {
					KnowledgePackage pkg = JSON.parseObject(line, KnowledgePackage.class);
					String newUuid = IdUtils.UUID();
					pkgMappings.put(pkg.getUuid(), newUuid);
					pkg.setUuid(newUuid);
					pkg.setProjectUuid(project.getUuid());
					pkg.setCreator(UserHolder.currentUser());
					return pkg;
				}).collect(Collectors.toList());
		if (!pkgs.isEmpty()) {
			knowledgePackageMapper.insertBatch(Lists.reverse(pkgs));
		}
		return pkgMappings;
	}
}
