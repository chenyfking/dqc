package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.beagledata.common.Result;
import com.beagledata.gaea.common.ValidateUtils;
import com.beagledata.gaea.ruleengine.annotation.PassingDirection;
import com.beagledata.gaea.ruleengine.util.PackageUtils;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.entity.AiModel;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.AssetsVersion;
import com.beagledata.gaea.workbench.entity.Folder;
import com.beagledata.gaea.workbench.entity.FunctionDefinition;
import com.beagledata.gaea.workbench.entity.Project;
import com.beagledata.gaea.workbench.entity.RecycleBin;
import com.beagledata.gaea.workbench.entity.Refer;
import com.beagledata.gaea.workbench.exception.InputNotMatchException;
import com.beagledata.gaea.workbench.mapper.AiModelMapper;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.mapper.AssetsTemplateMapper;
import com.beagledata.gaea.workbench.mapper.FolderMapper;
import com.beagledata.gaea.workbench.mapper.FunctionDefinitionMapper;
import com.beagledata.gaea.workbench.mapper.KnowledgePackageMapper;
import com.beagledata.gaea.workbench.mapper.ProjectMapper;
import com.beagledata.gaea.workbench.mapper.RecycleBinMapper;
import com.beagledata.gaea.workbench.mapper.ReferMapper;
import com.beagledata.gaea.workbench.rule.define.Constant;
import com.beagledata.gaea.workbench.rule.define.Drl;
import com.beagledata.gaea.workbench.rule.define.Fact;
import com.beagledata.gaea.workbench.rule.parser.DrlParser;
import com.beagledata.gaea.workbench.rule.parser.Parser;
import com.beagledata.gaea.workbench.rule.parser.ParserFactory;
import com.beagledata.gaea.workbench.rule.util.FactUtils;
import com.beagledata.gaea.workbench.rule.verifer.RuleVerifier;
import com.beagledata.gaea.workbench.rule.verifer.RuleVerifierImpl;
import com.beagledata.gaea.workbench.service.AssetsService;
import com.beagledata.gaea.workbench.util.RandomCode;
import com.beagledata.gaea.workbench.util.UploadFileClassLoader;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.gaea.workbench.util.UserSessionManager;
import com.beagledata.gaea.workbench.vo.AssetsReferenceVO;
import com.beagledata.gaea.workbench.vo.VerifierResultVO;
import com.beagledata.gaea.workbench.vo.VerifierVO;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @Auther: yinrj
 * @Date: 0018 2018/9/18 17:28
 * @Description: 资源文件管理
 */
@Service
public class AssetsServiceImpl extends BaseServiceImpl implements AssetsService {
	@Autowired
	private AssetsMapper assetsMapper;
	@Autowired
	private ProjectMapper projectMapper;
	@Autowired
	private UserSessionManager userSessionManager;
	@Autowired
	private AssetsTemplateMapper assetsTemplateMapper;
	@Autowired
	private FolderMapper folderMapper;
	@Autowired
	private RecycleBinMapper recycleBinMapper;
	@Autowired
	private ResourceResolver resourceResolver;
	@Autowired
	private KnowledgePackageMapper knowledgePackageMapper;
	@Autowired
	private AiModelMapper aiModelMapper;
	@Autowired
	private FunctionDefinitionMapper functionDefinitionMapper;
	@Autowired
	private ReferMapper referMapper;

	private final Pattern scriptFactPattern = Pattern.compile("Fact_(\\d+)");
	private final Pattern namePattern = Pattern.compile(".*(\\d+)");

	/**
	 * 条件查询资源文件列表
	 * @param page 当前页码
	 * @param pageNum 每页行数
	 * @param params 查询参数
	 * @return
	 */
	@Override
	public List<Assets> listOfParams(int page, int pageNum, Map params) {
		page = page == 0 ? 1 : page;
		pageNum = pageNum == 0 ? Integer.parseInt(Constants.PAGE_ROWS) : pageNum;
		params.put("params1", pageNum * (page - 1));
		params.put("params2", pageNum);
		params.put("isAdmin", UserHolder.hasAdminPermission());
		try {
			return assetsMapper.selectByParams(params);
		} catch (Exception e) {
			logger.error("查询资源文件列表异常：" + e.getLocalizedMessage(), e);
			throw e;
		}
	}

	/**
	 * 查询资源文件列表总数
	 * @param params
	 */
	@Override
	public int getTotal(Map params) {
		params.put("isAdmin", UserHolder.hasAdminPermission());
		return assetsMapper.selectCountByParams(params);
	}

	/**
	 * @auto: yangyongqiang
	 * @Description: 创建资源
	 * @Date: 2018-09-18 17:16
	 **/
	@Transactional
	@Override
	public void addAssets(Assets assets) {
		if (StringUtils.isBlank(assets.getName())) {
			throw new IllegalArgumentException("资源文件名称为空");
		}
		if (StringUtils.isBlank(assets.getProjectUuid())) {
			throw new IllegalArgumentException("所属项目uuid为空");
		}
		if (StringUtils.isBlank(assets.getType())) {
			throw new IllegalArgumentException("文件类型为空");
		}
		if (StringUtils.isBlank(assets.getDirParentId())) {
			assets.setDirParentId("0");
		}
		if (StringUtils.isNotBlank(assets.getDescription()) && assets.getDescription().length() > 100) {
			throw new IllegalArgumentException("描述不能超过100个字符");
		}

		try {
			assets.setUuid(IdUtils.UUID());
			assets.setCreator(UserHolder.currentUser());
			assetsMapper.insert(assets);
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("名称不能重复");
		} catch (Exception e) {
			logger.error("创建资源文件失败. assets: {}", assets, e);
			throw new IllegalStateException("创建资源文件失败");
		}
	}

	/**
	 * @auto: yangyongqiang
	 * @Description: 修改资源文件
	 * @Date: 2018-09-18 17:16
	 **/
	@Override
	public void editAssets(Assets assets) {
		if (StringUtils.isBlank(assets.getUuid())) {
			throw new IllegalStateException("uuid为空");
		}
		if (assetsMapper.assetsIsLocked(assets.getUuid())) {
			throw new IllegalArgumentException("文件被锁定，不能修改");
		}
		String dirParentId = assets.getDirParentId();
		if (StringUtils.isBlank(dirParentId) || (!"0".equals(dirParentId) && dirParentId.length() != 32)) {
			assets.setDirParentId("0");
		}

		try {
			Assets oldAssets = assetsMapper.selectByUuid(assets.getUuid());
			if (oldAssets == null) {
				throw new IllegalArgumentException("资源文件不存在");
			}

			assetsMapper.update(assets);
			//修改项目最后修改时间
			projectMapper.update(new Project(oldAssets.getProjectUuid()));
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("名称不能重复");
		} catch (IllegalArgumentException | IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			logger.error("修改资源文件失败. uuid: {}", assets.getUuid(), e);
			throw new IllegalStateException("修改资源文件失败");
		}
	}

	/**
	 * @auto: yinrj
	 * @Description: 更新编辑人信息
	 * @Date: 2020/4/29
	 **/
	@Override
	public Result updateEditor(String assetsUuids, boolean lock, String userUuid) {
		//lock=true , assetsUuids不为空  锁定一或多个资源
		//lock==false, assetsUuids不为空，解锁一或多个资源，assetsUuids为空，解锁当前用户锁定的所有资源
		//lock==false, userUuid为空，解锁所有资源

		try {
			//锁定文件
			if (lock) {
				if (StringUtils.isBlank(assetsUuids)) {
					throw new IllegalArgumentException("参数缺失，没有提供要锁定的资源文件编号");
				}
				String[] uuids = assetsUuids.split(",");
				if (uuids.length > 1) {
					throw new IllegalArgumentException("不能同时编辑锁定多个资源文件");
				}

				Assets editorInfo = assetsMapper.selectEditor(uuids[0]);
				if (editorInfo == null) {
					throw new IllegalArgumentException("资源文件不存在");
				}

				if (editorInfo.getEditor() == null) {
					//没有其他编辑者，锁定文件
					assetsMapper.updateEditor(uuids, userUuid, true);
					return Result.newSuccess();
				}
				if (editorInfo.getEditor().getUuid().equals(userUuid)) {
					//重复锁定
					return Result.newSuccess();
				}

				if (userSessionManager.sessionEffective(editorInfo.getEditor().getUuid())) {
					StringBuilder msg = new StringBuilder("操作失败");
					msg.append("，当前文件正在被").append(editorInfo.getEditor().getRealname()).append("编辑");
					if (editorInfo.getEditTime() != null) {
						String date = DateFormatUtils.format(editorInfo.getEditTime(), Constants.DEFAULT_DATE_FORMAT);
						msg.append("，编辑时间：").append(date);
					}
					return Result.newError().withMsg(msg.toString());
				}

				//编辑人session无效，强制修改编辑人
				assetsMapper.updateEditor(uuids, userUuid, lock);
				return Result.newSuccess();
			}

			if (StringUtils.isBlank(userUuid)) {
				assetsMapper.updateEditor(null, null, false);
				return Result.newSuccess();
			}
			//解锁文件编辑
			if (StringUtils.isBlank(assetsUuids)) {
				//解锁当所有资源文件
				assetsMapper.updateEditor(null, userUuid, false);
				return Result.newSuccess();
			}
			//解锁当前用户锁定的指定编号的资源文件
			assetsMapper.updateEditor(assetsUuids.split(","), userUuid, false);
			return Result.newSuccess();
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new IllegalStateException("更新编辑人信息失败");
		}
	}

	/**
	 * @auto: yangyongqiang
	 * @Description: 保存资源文件
	 * @Date: 2018-09-18 17:16
	 **/
	@Override
	@Transactional
	public Result saveContent(Assets assets) {
		if (StringUtils.isBlank(assets.getUuid())) {
			throw new IllegalArgumentException("文件uuid不能为空");
		}
		Boolean ok = assetsMapper.assetsIsLocked(assets.getUuid());
		if (ok == null) {
			throw new IllegalArgumentException("资源文件不存在");
		}
		if (ok) {
			throw new IllegalArgumentException("文件被锁定，不能修改和保存");
		}

		try {
			Assets dbAssets = assetsMapper.selectByUuid(assets.getUuid());
			if (null == dbAssets) {
				throw new IllegalArgumentException("资源文件不存在");
			}

			if (AssetsType.FACT.equals(dbAssets.getType())) {
				// 数据模型字段判断是否是java关键字
				String correct = "correct";
				String jkResult = FactUtils.validateJavaKeyword(correct, assets.getContent());
				if (!jkResult.equals(correct)) {
					assets.setContent(jkResult);
				}
			}
			String type = dbAssets.getType();
			Set<Refer> refers = null;
			if (!AssetsType.CONSTANT.equals(type)) {
				assets.setType(type);
				refers = getRefers(assets);
				List<String> types = Arrays.asList("func", "model", "thirdApi");
				if (CollectionUtils.isNotEmpty(refers)) {
					Set<String> uuids = refers.stream().filter(refer -> !types.contains(refer.getSubjectType())).map(Refer::getSubjectUuid).collect(Collectors.toSet());
					if (CollectionUtils.isNotEmpty(uuids)) {
						int count = assetsMapper.selectAssetsCountByUuids(uuids);
						if (count != uuids.size()) {
							return Result.newError().withMsg("使用的资源文件被删除，请确认文件内容");
						}
					}
				}
			}
			//枚举和数据模型判断字段是否能够修改
			if (AssetsType.CONSTANT.equals(type) || AssetsType.FACT.equals(type)) {
				String fieldLabel = factContentCanEdited(dbAssets, assets);
				if (StringUtils.isNotBlank(fieldLabel)) {
					return Result.newError().withMsg("字段[" + fieldLabel + "]被使用，无法修改或删除");
				}
			}
			if (AssetsType.FACT.equals(type)) {
				Fact fact = JSON.parseObject(assets.getContent(), Fact.class);
				assets.setEnName(fact.getEnName());
			}

			assetsMapper.update(assets);
			projectMapper.update(new Project(dbAssets.getProjectUuid()));
			Refer deleteRefer = new Refer();
			deleteRefer.setReferType(assets.getType());
			deleteRefer.setReferUuid(assets.getUuid());
			deleteRefer.setReferVersion(-1);
			referMapper.delete(deleteRefer);
			if (CollectionUtils.isNotEmpty(refers)) {
				referMapper.insertBatch(refers);
			}
			return Result.newSuccess();
		} catch (DuplicateKeyException e) {
			return Result.newError().withMsg("模型英文模型不能重复");
		} catch (InputNotMatchException e) {
			throw e;
		} catch (Exception e) {
			logger.error("保存资源文件内容出错. uuid: {}", assets.getUuid(), e);
			throw new IllegalStateException("保存资源文件内容失败");
		}
	}


	/**
	 * 描述: 数据模型贩毒案字段是否能够修改
	 * @param: [oldAssets, newAssets]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.lang.String
	 */
	private String factContentCanEdited(Assets oldAssets, Assets newAssets) {
		String oldContent = oldAssets.getContent();
		String newContent = newAssets.getContent();
		//内容都为空，可以修改
		if (StringUtils.isBlank(oldContent) && StringUtils.isBlank(newContent)) {
			return null;
		}

		//判断是否引用
		if (StringUtils.isBlank(oldContent)) {
			return null;
		}
		if (oldContent.equals(newContent)) {
			return null;
		}
		Map<String, com.beagledata.gaea.workbench.rule.define.Field> oldFieldMap = getFieldMap(oldAssets);
		newAssets.setId(oldAssets.getId());
		newAssets.setUuid(oldAssets.getUuid());
		newAssets.setName(oldAssets.getName());
		Map<String, com.beagledata.gaea.workbench.rule.define.Field> newFieldMap = getFieldMap(newAssets);
		for (Map.Entry<String, com.beagledata.gaea.workbench.rule.define.Field> entry : oldFieldMap.entrySet()) {
			String key = entry.getKey();
			com.beagledata.gaea.workbench.rule.define.Field oldField = entry.getValue();
			com.beagledata.gaea.workbench.rule.define.Field newField = newFieldMap.get(key);
			if (newField == null) {
				Integer countNum = referMapper.countBySubjectUuidAndChild(oldAssets.getUuid(), key);
				if (countNum > 0) {
					return oldField.getLabel();
				}
				continue;
			}
			String oldType = oldField.getType().name();
			String newType = newField.getType().name();
			String oldSubType = oldField.getSubType();
			String newSubType = newField.getSubType();
			newSubType = newSubType == null ? "" : newSubType;
			oldSubType = oldSubType == null ? "" : oldSubType;
			boolean flag = oldType.equals(newType);
			boolean flag1 = oldSubType.equals(newSubType);
			if (!flag || (flag && !flag1)) {
				Integer countNum = referMapper.countBySubjectUuidAndChild(oldAssets.getUuid(), key);
				if (countNum > 0) {
					return oldField.getLabel();
				}
			}
		}
		return null;
	}

	/**
	 * 描述: 获取字段id 与字段的映射关系
	 * @param: [assets]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.util.Map<java.lang.String, com.beagledata.gaea.workbench.rule.define.Field>
	 */
	private Map<String, com.beagledata.gaea.workbench.rule.define.Field> getFieldMap(Assets assets) {
		Map<String, com.beagledata.gaea.workbench.rule.define.Field> fieldMap = new HashMap<>();
		if (StringUtils.isBlank(assets.getContent())) {
			return fieldMap;
		}
		Fact fact = Fact.fromAssets(assets);
		List<com.beagledata.gaea.workbench.rule.define.Field> fields = fact.getFields();
		for (com.beagledata.gaea.workbench.rule.define.Field field : fields) {
			fieldMap.put(field.getId(), field);
		}
		return fieldMap;
	}

	/**
	 * @auto: yangyongqiang
	 * @Description: 删除资源文件
	 * @Date: 2018-09-18 17:16
	 **/
	@Override
	@Transactional
	public Result delete(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			throw new IllegalStateException("uuid为空");
		}

		Assets assets = assetsMapper.selectByUuid(uuid);
		if (assets == null) {
			throw new IllegalArgumentException("资源文件不存在");
		}
		if (assets.isLocked()) {
			throw new IllegalArgumentException("文件被锁定，不能删除");
		}

		try {
			Integer countNum = referMapper.countBySubjectUuid(uuid);
			if (countNum > 0) {
				return Result.newError().withMsg("资源文件被使用，无法删除");
			}

			assetsMapper.deleteByUuid(uuid);
			//回收站增加记录
			RecycleBin recycleBin = new RecycleBin();
			recycleBin.setUuid(IdUtils.UUID());
			recycleBin.setProjectUuid(assets.getProjectUuid());
			recycleBin.setAssetsUuid(assets.getUuid());
			recycleBin.setAssetsName(assets.getName());
			recycleBin.setAssetsType(assets.getType());
			recycleBin.setCreator(UserHolder.currentUser());
			recycleBinMapper.insert(recycleBin);
			return Result.newSuccess();
		} catch (Exception e) {
			logger.error(uuid + e.getLocalizedMessage(), e);
			throw new IllegalStateException("删除资源文件失败");
		}
	}

	/**
	 * @auto: yangyongqiang
	 * @Description: 查询资源文件详情
	 * @Date: 2018-09-18 17:16
	 **/
	@Override
	public Result selectAssets(String uuid, int versionNo) {
		if (StringUtils.isBlank(uuid)) {
			throw new IllegalArgumentException("uuid为空");
		}

		try {
			Assets assets;
			if (versionNo < 1) {
				assets = assetsMapper.selectByUuid(uuid);
			} else {
				assets = assetsMapper.selectVersion(uuid, versionNo);
			}
			if (assets == null) {
				return Result.newError().withMsg("资源文件不存在");
			}
			return Result.newSuccess().withData(assets);
		} catch (Exception e) {
			logger.error("查询资源文件详情失败. uuid: {}, versionNo: {}", uuid, versionNo, e);
			throw new IllegalStateException("查询资源文件详情失败");
		}
	}

	@Override
	public Result selectContentById(int id, Integer versionNo) {
		if (id <= 0) {
			throw new IllegalArgumentException("参数有误");
		}
		try {
			Assets assets = assetsMapper.selectByIdAndVersion(id, versionNo);
			if (null == assets) {
				logger.info("根据id查询版本文件不存在, id = {}", id);
				return Result.newError().withMsg("文件不存在");
			}
			return Result.newSuccess().withData(assets.getContent());
		} catch (Exception e) {
			logger.error("根据id查询文件内容出错. id: {}", id, e);
			throw new IllegalStateException("查询失败");
		}
	}

	/**
	 * @author yinrj
	 * @params projectUuid
	 * @Description 根据项目uuid查询资源文件类别集合
	 * @date 0021 2018/9/21 17:41
	 */
	@Override
	public Set<String> listTypeGroupByProjectUuid(String projectUuid) {
		try {
			return assetsMapper.selectTypeGroupByProjectUuid(projectUuid, UserHolder.hasAdminPermission(), AssetsType.TEST_CASE);
		} catch (Exception e) {
			logger.error("分组查询资源文件列表异常", e);
			throw new IllegalStateException("分组查询资源文件列表异常");
		}
	}

	/**
	 * @author yinrj
	 * @params [projectUuid, tag]
	 * @Description 根据项目uuid和标签查询资源文件列表
	 * @date 0021 2018/9/21 17:41
	 */
	@Override
	public List<Assets> listByProjectUuid(String projectUuid) {
		try {
			return assetsMapper.selectByProjectUuid(projectUuid, UserHolder.hasAdminPermission());
		} catch (Exception e) {
			logger.error("查询资源文件列表异常.", e);
			throw new IllegalStateException("查询资源文件列表异常");
		}
	}

	@Override
	public Map listBomByProjectUuid(String projectUuid) {
		if (StringUtils.isBlank(projectUuid)) {
			throw new IllegalArgumentException("参数不能为空");
		}

		Map<String, List> map = new HashMap<>(2);
		List<Fact> facts = new ArrayList<>();
		List<Constant> constants = new ArrayList<>();
		map.put("facts", facts);
		map.put("constants", constants);
		try {
			List<Assets> assetsList = assetsMapper.selectBomByProjectUuid(projectUuid);
			for (Assets assets : assetsList) {
				if (StringUtils.isBlank(assets.getContent())) {
					continue;
				}

				if (AssetsType.FACT.equals(assets.getType())) {
					facts.add(Fact.fromAssets(assets));
				} else {
					constants.add(Constant.fromAssets(assets));
				}
			}
		} catch (Exception e) {
			logger.error("获取项目: {}，数据模型、常量有误. ", projectUuid, e);
		}
		return map;
	}

	@Override
	public Result lockAssets(String uuid, Boolean isLock) {
		if (StringUtils.isBlank(uuid) || isLock == null) {
			throw new IllegalArgumentException("参数不能为空");
		}

		try {
			Assets lockInfo = assetsMapper.selectLockInfo(uuid);
			if (lockInfo == null) {
				return Result.newError().withMsg("资源文件不存在");
			}
			if (isLock && lockInfo.isLocked()) {
				//锁定文件
				return Result.SUCCESS;
			}

			if (!isLock && !lockInfo.isLocked() && lockInfo.getEditor() == null) {
				//已经解锁文件
				return Result.SUCCESS;
			}
			String userUuid = UserHolder.currentUserUuid();
			if (!isLock && !UserHolder.hasAdminPermission() && lockInfo.getEditor() != null
					&& !lockInfo.getLocker().getUuid().equals(userUuid)) {
				return Result.newError().withMsg(String.format("文件已被%s在%s锁定,请联系该用户或管理员解锁",
						lockInfo.getLocker().getRealname(), new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).format(lockInfo.getLockTime())));
			}
			if (isLock) {
				assetsMapper.updateLock(uuid, isLock, userUuid, new Date());
			} else {
				assetsMapper.updateLock(uuid, isLock, null, null);
			}
			return Result.newSuccess();
		} catch (Exception e) {
			logger.error("锁定资源文件锁失败. uuid: {}, lock: {}", uuid, isLock, e);
			return Result.ERROR;
		}
	}

	@Override
	public Result listOfAssetNameAndFolderName(Assets assets) {
		if (assets == null || StringUtils.isBlank(assets.getProjectUuid())) {
			throw new IllegalStateException("参数不能为空");
		}

		List<String> types = new ArrayList<>();
		types.add(AssetsType.GUIDED_RULE);
		types.add(AssetsType.RULE_SCRIPT);
		types.add(AssetsType.RULE_TABLE);
		types.add(AssetsType.RULE_TREE);
		types.add(AssetsType.SCORECARD);
		types.add(AssetsType.RULE_FLOW);

		List<AssetsReferenceVO> assetsList = assetsMapper.selectByAssetNameAndFolderName(
				assets.getProjectUuid(),
				assets.getName(),
				UserHolder.hasAdminPermission()
		);
		List dataList = new ArrayList();
		for (String type : types) {
			Map map = new HashMap();
			List<AssetsReferenceVO> list = new ArrayList<>();
			map.put("type", type);
			for (AssetsReferenceVO assets1 : assetsList) {
				if (type.equals(assets1.getType())) {
					list.add(assets1);
				}
			}
			map.put("list", list);
			dataList.add(map);
		}
		return Result.newSuccess().withData(dataList);
	}

	@Override
	public Result getCompareContent(String assetUuid, String assetType, Integer baseVersion, Integer compareVersion) {
		if (StringUtils.isBlank(assetUuid) || StringUtils.isBlank(assetType) || baseVersion == null || compareVersion == null) {
			throw new IllegalArgumentException("参数不能为空");
		}
		try {
			Assets baseAssets = assetsMapper.selectVersion(assetUuid, baseVersion);
			Assets compareAssets = assetsMapper.selectVersion(assetUuid, compareVersion);
			if ("guidedrule".equals(assetType)) {
				return compareGuidedRule(baseAssets, compareAssets, assetType);
			}
			if ("ruletable".equals(assetType)) {
				return compareRuleTable(baseAssets, compareAssets, assetType);
			}
			if ("ruletree".equals(assetType)) {
				//todo 待修改,
				return compareRuleTree(baseAssets, compareAssets, assetType);
			}
			JSONObject finalBase = new JSONObject();
			finalBase.put("content", baseAssets.getContent());
			finalBase.put("type", baseAssets.getType());
			JSONObject finalCompare = new JSONObject();
			finalCompare.put("content", compareAssets.getContent());
			finalCompare.put("type", compareAssets.getType());
			Map<String, JSONObject> res = new HashMap<>();
			res.put("baseVersion", finalBase);
			res.put("compareVersion", finalCompare);
			return Result.newSuccess().withData(res);
		} catch (Exception e) {
			logger.error("版本比较失败: assetUuid = {}, assetType = {},  baseVersion = {}, compareVersion = {} ", assetUuid, assetType, baseVersion, compareVersion, e);
			throw new IllegalStateException("版本比较失败");
		}
	}

	/**
	 * 描述: 向导式决策集版本比较
	 * @param: [baseAssets, compareAssets] 基础版本 ，比较版本
	 * @author: 周庚新
	 * @date: 2020/7/15
	 * @return: com.beagledata.common.Result
	 */
	private Result compareGuidedRule(Assets baseAssets, Assets compareAssets, String assetsType) {
		String baseContent = baseAssets.getContent();
		String compareContent = compareAssets.getContent();
		JSONObject baseContentObject = JSONObject.parseObject(baseContent);
		JSONObject compareContentObj = JSONObject.parseObject(compareContent);
		JSONArray baseArr = baseContentObject.getJSONArray("rules");
		JSONArray compareArr = compareContentObj.getJSONArray("rules");
		List<String> baseNames = new ArrayList<>();
		Map<String, JSONObject> baseMap = new HashMap<>();
		for (int i = 0; i < baseArr.size(); i++) {
			JSONObject object = baseArr.getJSONObject(i);
			String name = object.getString("name");
			baseNames.add(name);
			baseMap.put(name, object);
		}
		List<String> compareNames = new ArrayList<>();
		Map<String, JSONObject> compareMap = new HashMap<>();
		for (int i = 0; i < compareArr.size(); i++) {
			JSONObject object = compareArr.getJSONObject(i);
			String name = object.getString("name");
			compareNames.add(name);
			compareMap.put(name, object);
		}
		List<String> all = new ArrayList<>();
		all.addAll(baseNames);
		int noContain = 0;
		for (int i = 0; i < compareNames.size(); i++) {
			String name = compareNames.get(i);
			if (all.contains(name)) {
				int index = all.indexOf(name);
				for (int m = noContain; m < i; m++) {
					all.add(index, compareNames.get(m));
					index = index + 1;
				}
				noContain = i + 1;
			}
		}
		if (noContain < compareNames.size()) {
			for (int m = noContain; m < compareNames.size(); m++) {
				all.add(compareNames.get(m));
			}
		}
		int size = all.size();
		List<JSONObject> baseResultList = Arrays.asList(new JSONObject[size]);
		List<JSONObject> compareReultList = Arrays.asList(new JSONObject[size]);
		for (int i = 0; i < size; i++) {
			String name = all.get(i);
			JSONObject baseObject = baseMap.get(name);
			JSONObject compareObject = compareMap.get(name);
			boolean flag1 = baseObject != null;
			boolean flag2 = compareObject != null;
			if (flag1 && flag2) {
				if (baseObject.toJSONString().equals(compareObject.toJSONString())) {
					baseObject.put("cssStyle", "same");
					compareObject.put("cssStyle", "same");
				} else {
					baseObject.put("cssStyle", "diff");
					compareObject.put("cssStyle", "diff");
				}
				baseResultList.set(i, baseObject);
				compareReultList.set(i, compareObject);
				continue;
			}
			if (flag1) {
				baseObject.put("cssStyle", "del");
				baseResultList.set(i, baseObject);
				continue;
			}
			if (flag2) {
				compareObject.put("cssStyle", "add");
				compareReultList.set(i, compareObject);
				continue;
			}
		}
		// 删除null 后续有要求类似基线版本比较显示注意修改
		baseResultList = baseResultList.stream().filter(Objects::nonNull).collect(Collectors.toList());
		compareReultList = compareReultList.stream().filter(Objects::nonNull).collect(Collectors.toList());
		Map<String, List> baseRes = new HashMap<>();
		Map<String, List> compareRes = new HashMap<>();
		baseRes.put("rules", baseResultList);
		compareRes.put("rules", compareReultList);
		JSONObject finalBase = new JSONObject();
		finalBase.put("content", JSONObject.toJSONString(baseRes));
		finalBase.put("type", assetsType);
		JSONObject finalCompare = new JSONObject();
		finalCompare.put("content", JSONObject.toJSONString(compareRes));
		finalCompare.put("type", assetsType);
		Map<String, JSONObject> map = new HashMap<>();
		map.put("baseVersion", finalBase);
		map.put("compareVersion", finalCompare);
		return Result.newSuccess().withData(map);
	}

	/**
	 * 描述: 决策表版本比较
	 * @param: [baseAssets, compareAssets, assetsType]
	 * @author: 周庚新
	 * @date: 2020/7/16
	 * @return: com.beagledata.common.Result
	 */
	private Result compareRuleTable(Assets baseAssets, Assets compareAssets, String assetsType) {
		JSONObject baseContentObject = JSON.parseObject(baseAssets.getContent());
		JSONObject compareContentObject = JSON.parseObject(compareAssets.getContent());
		JSONArray baseHeaderArray = baseContentObject.getJSONArray("headers");
		JSONArray compareHeaderArray = compareContentObject.getJSONArray("headers");
		compareAndAddStyle(baseHeaderArray, compareHeaderArray);
		baseContentObject.put("headers", baseHeaderArray);
		compareContentObject.put("headers", compareHeaderArray);
		JSONObject finalBase = new JSONObject();
		finalBase.put("content", JSONObject.toJSONString(baseContentObject));
		finalBase.put("type", assetsType);
		JSONObject finalCompare = new JSONObject();
		finalCompare.put("content", JSONObject.toJSONString(compareContentObject));
		finalCompare.put("type", assetsType);
		Map<String, JSONObject> map = new HashMap<>();
		map.put("baseVersion", finalBase);
		map.put("compareVersion", finalCompare);
		return Result.newSuccess().withData(map);
	}

	private Result compareRuleTree(Assets baseAssets, Assets compareAssets, String assetsType) {
		JSONObject baseContentObject = JSON.parseObject(baseAssets.getContent());
		JSONObject compareContentObject = JSON.parseObject(compareAssets.getContent());
		JSONArray baseAttrArray = baseContentObject.getJSONArray("attrs");
		JSONArray compareAttrArray = compareContentObject.getJSONArray("attrs");
		//属性比较
		compareAndAddStyle(baseAttrArray, compareAttrArray);

		JSONObject baseTreeObject = baseContentObject.getJSONObject("tree");
		JSONObject compareTreeObject = compareContentObject.getJSONObject("tree");

		//判断树初始设置条件变量是否一致
		JSONObject baseTreeFact = baseTreeObject.getJSONObject("fact");
		JSONObject compareTreeFact = compareTreeObject.getJSONObject("fact");
		boolean factFlag = baseTreeFact.toJSONString().equals(compareTreeFact.toJSONString());
		//获取每一条路径，即一个规则
		Map<String, Map<String, JSONArray>> baseCondAndAct = new HashMap<>();
		Stack<JSONObject> baseStack = new Stack<>();
		getTreeCondAndAct(baseTreeObject, baseStack, baseCondAndAct, factFlag);
		Map<String, Map<String, JSONArray>> compareCondAndAct = new HashMap<>();
		Stack<JSONObject> compareStack = new Stack<>();
		getTreeCondAndAct(compareTreeObject, compareStack, compareCondAndAct, factFlag);

		if (!factFlag) {
			baseTreeFact.put("cssStyle", "diff");
			compareTreeFact.put("cssStyle", "diff");
			JSONObject finalBase = new JSONObject();
			finalBase.put("content", JSONObject.toJSONString(baseContentObject));
			finalBase.put("type", assetsType);
			JSONObject finalCompare = new JSONObject();
			finalCompare.put("content", JSONObject.toJSONString(compareContentObject));
			finalCompare.put("type", assetsType);
			Map<String, JSONObject> map = new HashMap<>();
			map.put("baseVersion", finalBase);
			map.put("compareVersion", finalCompare);
			return Result.newSuccess().withData(map);
		}

		baseTreeFact.put("cssStyle", "same");
		compareTreeFact.put("cssStyle", "same");

		//比较每一条路径，即一个规则，根据条件判断是否为新增，减少，条件相同则使用动作比较，最终结果在动作展示
		Set<String> baseCons = baseCondAndAct.keySet();
		Set<String> compareCons = compareCondAndAct.keySet();
		String baseConStr = JSONObject.toJSONString(baseCons);
		String compareConStr = JSONObject.toJSONString(compareCons);
		Set<String> sameSet = JSONArray.parseObject(baseConStr, Set.class);
		Set<String> onlyBase = JSONArray.parseObject(baseConStr, Set.class);
		Set<String> onlyCompare = JSONArray.parseObject(compareConStr, Set.class);
		sameSet.retainAll(compareCons);
		onlyBase.removeAll(compareCons);
		onlyCompare.removeAll(baseCons);
		treeAddStyle(sameSet, baseCondAndAct, compareCondAndAct, "same");
		treeAddStyle(onlyBase, baseCondAndAct, compareCondAndAct, "del");
		treeAddStyle(onlyCompare, baseCondAndAct, compareCondAndAct, "add");

		JSONObject finalBase = new JSONObject();
		finalBase.put("content", JSONObject.toJSONString(baseContentObject));
		finalBase.put("type", assetsType);
		JSONObject finalCompare = new JSONObject();
		finalCompare.put("content", JSONObject.toJSONString(compareContentObject));
		finalCompare.put("type", assetsType);
		Map<String, JSONObject> map = new HashMap<>();
		map.put("baseVersion", finalBase);
		map.put("compareVersion", finalCompare);
		return Result.newSuccess().withData(map);
	}


	/**
	 * 描述: 遍历决策树的每一条根节点到叶子节点的路径，将相同条件的放到同一个对象list中，便于比较
	 * @param: [n, pathStack, res]
	 * @author: 周庚新
	 * @date: 2020/7/20
	 * @return: void
	 */
	public void getTreeCondAndAct(JSONObject tree, Stack<JSONObject> pathStack, Map<String, Map<String, JSONArray>> res, boolean factFlag) {
		//入栈
		pathStack.push(tree);
		JSONArray childlist = tree.getJSONArray("children");
		//没有children 说明是叶子结点
		if (childlist == null) {
			//该路径上的条件节点
			JSONArray conditions = new JSONArray();
			//该路径上所有的动作节点
			JSONArray actions = new JSONArray();
			Iterator stackIt = pathStack.iterator();
			while (stackIt.hasNext()) {
				JSONObject o = (JSONObject) stackIt.next();
				if ("CONDITION".equals(o.getString("nodeType"))) {
					JSONObject condition = o.getJSONObject("condition");
					if (condition != null) {
						if (!factFlag) {
							// 整体不一致时，条件和动作增加 same
							condition.put("cssStyle", "same");
						}
						conditions.add(condition);
					}
				} else if ("ACTION".equals(o.getString("nodeType"))) {
					JSONObject action = o.getJSONObject("rhs");
					if (action != null) {
						if (!factFlag) {
							// 整体不一致时，条件和动作增加 same
							action.put("cssStyle", "same");
						}
						actions.add(action);
					}
				}
			}
			// 条件 MD5 生成，是具有相同条件的动作能够在同一个list中
			String uuid = DigestUtils.md5Hex(conditions.toJSONString());
			Map<String, JSONArray> map = res.get(uuid);
			if (map == null) {
				map = new HashMap<>();
			} else {
				actions.addAll(map.get("actions"));
			}
			map.put("conditions", conditions);
			map.put("actions", actions);
			res.put(uuid, map);
			return;
		} else {
			Iterator it = childlist.iterator();
			while (it.hasNext()) {
				JSONObject child = (JSONObject) it.next();
				//深度优先 进入递归
				getTreeCondAndAct(child, pathStack, res, factFlag);
				//回溯时候出栈
				pathStack.pop();
			}
		}
	}


	/**
	 * 描述: 决策树添加样式比较并添加样式字段
	 * @param: [baseArray, compareArray]
	 * @author: 周庚新
	 * @date: 2020/7/16
	 * @return: void
	 */
	private void treeAddStyle(Set<String> keys, Map<String, Map<String, JSONArray>> baseCondAndAct, Map<String, Map<String, JSONArray>> compareCondAndAct, String cssStyle) {
		if ("add".equals(cssStyle)) {
			for (String key : keys) {
				Map<String, JSONArray> conAct = compareCondAndAct.get(key);
				Collection<JSONArray> conActArr = conAct.values();
				conActArr.forEach(c -> {
					for (int i = 0; i < c.size(); i++) {
						JSONObject object = c.getJSONObject(i);
						object.put("cssStyle", cssStyle);
					}
				});
			}
			return;
		}
		if ("del".equals(cssStyle)) {
			for (String key : keys) {
				Map<String, JSONArray> conAct = baseCondAndAct.get(key);
				Collection<JSONArray> conActArr = conAct.values();
				conActArr.forEach(c -> {
					for (int i = 0; i < c.size(); i++) {
						JSONObject object = c.getJSONObject(i);
						object.put("cssStyle", cssStyle);
					}
				});
			}
			return;
		}
		if ("same".equals(cssStyle)) {
			for (String key : keys) {
				// 条件设置相同的cssStyle
				Map<String, JSONArray> baseCondAndActMap = baseCondAndAct.get(key);
				JSONArray baseConArr = baseCondAndActMap.get("conditions");
				for (int i = 0; i < baseConArr.size(); i++) {
					JSONObject object = baseConArr.getJSONObject(i);
					object.put("cssStyle", cssStyle);
				}
				Map<String, JSONArray> compareCondAndActMap = compareCondAndAct.get(key);
				JSONArray comapareConArr = compareCondAndActMap.get("conditions");
				for (int i = 0; i < comapareConArr.size(); i++) {
					JSONObject object = comapareConArr.getJSONObject(i);
					object.put("cssStyle", cssStyle);
				}
				// 动作相互比较设置cssStyle
				JSONArray baseActionArr = baseCondAndActMap.get("actions");
				JSONArray compareActionArr = compareCondAndActMap.get("actions");
				compareAndAddStyle(baseActionArr, compareActionArr);

			}
		}
	}


	/**
	 * 描述: 比较并添加样式字段
	 * @param: [baseArray, compareArray]
	 * @author: 周庚新
	 * @date: 2020/7/16
	 * @return: void
	 */
	private void compareAndAddStyle(JSONArray baseArray, JSONArray compareArray) {
		//相同添加 same, base 有 compare无 为base的对象增加 del
		for (int i = 0; i < baseArray.size(); i++) {
			JSONObject baseObject = baseArray.getJSONObject(i);
			boolean same = false;
			for (int j = 0; j < compareArray.size(); j++) {
				JSONObject compareObject = compareArray.getJSONObject(j);
				if (baseObject.toJSONString().equals(compareObject.toJSONString())) {
					baseObject.put("cssStyle", "same");
					compareObject.put("cssStyle", "same");
					same = true;
					break;
				}
			}
			if (!same) {
				baseObject.put("cssStyle", "del");
			}
		}
		// base 无 compare 有 为base的对象增加 add
		for (int i = 0; i < compareArray.size(); i++) {
			JSONObject compareObject = compareArray.getJSONObject(i);
			boolean same = false;
			for (int j = 0; j < baseArray.size(); j++) {
				JSONObject baseObject = baseArray.getJSONObject(j);
				if (baseObject.toJSONString().equals(compareObject.toJSONString())) {
					baseObject.put("cssStyle", "same");
					compareObject.put("cssStyle", "same");
					same = true;
					break;
				}
			}
			if (!same) {
				compareObject.put("cssStyle", "add");
			}
		}
	}

	@Override
	@Transactional
	public void addNewVersion(Assets assets) {
		if (StringUtils.isBlank(assets.getUuid())) {
			throw new IllegalArgumentException("资源文件uuid不能为空");
		}
		if (assets.getVersionDesc() != null && assets.getVersionDesc().length() > 100) {
			throw new IllegalArgumentException("版本描述不能超过100个字符");
		}
		if (assetsMapper.assetsIsLocked(assets.getUuid())) {
			throw new IllegalArgumentException("文件被锁定");
		}

		assets.setCreator(UserHolder.currentUser());

		try {
			// 保存内容到当前记录
			assetsMapper.update(assets);
			// 增加一条新版本记录
			assetsMapper.insertNewVersion(assets);
			Assets assetsVersion = assetsMapper.selectVersionById(assets.getId());
			//删除关联
			Refer deleteRefer = new Refer();
			deleteRefer.setReferType(assets.getType());
			deleteRefer.setReferUuid(assets.getUuid());
			deleteRefer.setReferVersion(-1);
			referMapper.delete(deleteRefer);
			//添加当前编辑引用
			assets.setType(assetsVersion.getType());
			Set<Refer> refers = getRefers(assets);
			if (CollectionUtils.isNotEmpty(refers)) {
				referMapper.insertBatch(refers);
			}
			//添加版本引用
			Set<Refer> versionRefers = getRefers(assetsVersion);
			if (CollectionUtils.isNotEmpty(versionRefers)) {
				referMapper.insertBatch(versionRefers);
			}
		} catch (Exception e) {
			logger.error("保存新版本失败. assetsUuid: {}", assets.getUuid(), e);
			throw new IllegalStateException("保存新版本失败");
		}
	}

	@Override
	public Result listVersionsByUuid(String assetsUuid, int page, int pageNum) {
		try {
			int total = assetsMapper.countVersions(assetsUuid);
			if (total <= 0) {
				return Result.newSuccess().withTotal(0);
			}

			List<Assets> list = assetsMapper.selectVersions(assetsUuid, (page - 1) * pageNum, pageNum);
			return Result.newSuccess().withData(list).withTotal(total);
		} catch (Exception e) {
			logger.error(assetsUuid + ", " + e.getLocalizedMessage(), e);
		}
		return Result.newSuccess().withTotal(0);
	}

	@Override
	@Transactional
	public Result deleteVersion(Assets assets) {
		if (StringUtils.isBlank(assets.getUuid()) || assets.getVersionNo() == null) {
			throw new IllegalArgumentException("参数异常，请检查参数");
		}

		try {
			Assets deleteAssets = assetsMapper.selectByUuid(assets.getUuid());
			if (deleteAssets == null) {
				throw new IllegalArgumentException("资源文件不存在");
			}
			if (deleteAssets.isLocked()) {
				throw new IllegalArgumentException("文件被锁定");
			}
			Integer countNum = referMapper.countBySubjectUuidAndSubjectVersion(assets.getUuid(), assets.getVersionNo());
			if (countNum != null && countNum > 0) {
				return Result.newError().withMsg("该版本已被使用，无法删除");
			}
			Refer deleteRefer = new Refer();
			deleteRefer.setReferUuid(assets.getUuid());
			deleteRefer.setReferVersion(assets.getVersionNo());
			referMapper.delete(deleteRefer);
			assetsMapper.deleteVersion(assets);
			return Result.newSuccess();
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("删除版本失败, assetsUuid: {}, versionNo:{}", assets.getUuid(), assets.getVersionNo(), e);
			throw new IllegalStateException("删除版本失败");
		}
	}

	@Override
	@Transactional
	public Result changeVersion(Assets assets) {
		if (StringUtils.isBlank(assets.getUuid()) || assets.getVersionNo() == null) {
			throw new IllegalArgumentException("参数异常，请检查参数");
		}

		try {
			Boolean ok = assetsMapper.assetsIsLocked(assets.getUuid());
			if (ok == null) {
				throw new IllegalArgumentException("资源文件不存在");
			}
			if (ok) {
				throw new IllegalArgumentException("文件被锁定");
			}
			assetsMapper.updateFromVersion(assets);
			Assets newAssets = assetsMapper.selectByUuid(assets.getUuid());
			//删除关联
			Refer deleteRefer = new Refer();
			deleteRefer.setReferType(newAssets.getType());
			deleteRefer.setReferUuid(newAssets.getUuid());
			deleteRefer.setReferVersion(-1);
			referMapper.delete(deleteRefer);
			//添加当前编辑引用
			Set<Refer> refers = getRefers(newAssets);
			if (CollectionUtils.isNotEmpty(refers)) {
				referMapper.insertBatch(refers);
			}
			Map<String, Object> res = new HashMap<>();
			res.put("content", newAssets.getContent());
			return Result.newSuccess().withData(res);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error(assets + ", " + e.getLocalizedMessage(), e);
			throw new IllegalStateException("版本切换失败");
		}
	}

	@Override
	public Assets rulesCopy(Assets assets) {
		if (assets == null || assets.getUuid() == null) {
			logger.info("规则复制参数误");
			throw new IllegalArgumentException("参数有误");
		}

		assets = assetsMapper.selectByUuid(assets.getUuid());
		if (assets == null) {
			logger.info("规则复制查询资源文件为空");
			throw new IllegalArgumentException("文件不存在");
		}
		String newUuid = IdUtils.UUID();
		Assets newAssets = new Assets();
		newAssets.setName(copyName(assets.getName(), assets.getType(), assets.getProjectUuid()));
		newAssets.setUpdateTime(assets.getUpdateTime());
		newAssets.setDescription(assets.getDescription());
		String content = assets.getContent();
		String type = assets.getType();
		if (AssetsType.FACT.equals(type) || AssetsType.CONSTANT.equals(type)) {
			content = replaceFactId(content, new HashMap<>(), new HashMap<>());
		}
		if (AssetsType.FACT.equals(type)) {
			// 替换数据模型英文名称
			String enName = assets.getEnName();
			if (StringUtils.isNotBlank(enName)) {
				String newEnName = copyFactEnName(enName, assets.getProjectUuid());
				if (!enName.equals(newEnName)) {
					JSONObject object = JSON.parseObject(content);
					object.put("enName", newEnName);
					content = object.toJSONString();
				}
				assets.setEnName(newEnName);
			}
		}
		newAssets.setContent(content);
		newAssets.setType(type);
		newAssets.setLocked(assets.isLocked());
		newAssets.setVersionNo(0);
		newAssets.setUuid(newUuid);
		newAssets.setProjectUuid(assets.getProjectUuid());
		newAssets.setCreator(UserHolder.currentUser());
		newAssets.setDirParentId(assets.getDirParentId());
		Set<Refer> refers = getRefers(newAssets);
		if (CollectionUtils.isNotEmpty(refers)) {
			referMapper.insertBatch(refers);
		}
		assetsMapper.insert(newAssets);
		return newAssets;
	}

	/**
	 * 导入判断是否有重名
	 * @param name
	 * @param names
	 * @param length
	 * @return
	 */
	public String copyName(String name, List<String> names, Integer length) {
		if (!names.contains(name)) {
			return name;
		}
		if (name.length() > length) {
			name = name.substring(0, length);
		} else {
			Matcher matcher = namePattern.matcher(name);
			if (matcher.matches()) {
				String indexStr = matcher.group(1);
				int indexLength = indexStr.length();
				String oleName = name.substring(0, name.length() - indexLength);
				Integer index = NumberUtils.toInt(indexStr) + 1;
				name = oleName + index;
				while (name.length() > length) {
					name = oleName.substring(0, oleName.length() - 1) + index;
				}
			} else {
				if (name.length() == length) {
					name = name.substring(0, length - 1) + "2";
				} else {
					name = name + "2";
				}
			}
		}
		return copyName(name, names, length);
	}

	/**
	 * 描述: 数据库判断是否重名
	 * @param: [name, type, projectUuid]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: java.lang.String
	 */
	public String copyName(String name, String type, String projectUuid) {
		if (name.length() > 20) {
			name = name.substring(0, 20);
		}
		Assets assets = assetsMapper.selectAsset(name, type, projectUuid, 0);
		if (assets == null) {
			return name;
		}

		Matcher matcher = namePattern.matcher(name);
		if (matcher.matches()) {
			String indexStr = matcher.group(1);
			int indexLength = indexStr.length();
			String oleName = name.substring(0, name.length() - indexLength);
			Integer index = NumberUtils.toInt(indexStr) + 1;
			name = oleName + index;
			while (name.length() > 20) {
				name = oleName.substring(0, oleName.length() - 1) + index;
			}
		} else {
			if (name.length() == 20) {
				name = name.substring(0, 19) + "2";
			} else {
				name = name + "2";
			}
		}
		return copyName(name, type, projectUuid);
	}

	public String copyFactEnName(String enName, String projectUuid) {
		if (enName.length() > 50) {
			enName = enName.substring(0, 50);
		}
		Assets assets = assetsMapper.selectAssetsByEnName(enName, projectUuid);
		if (assets == null) {
			return enName;
		}

		Matcher matcher = namePattern.matcher(enName);
		if (matcher.matches()) {
			String indexStr = matcher.group(1);
			int indexLength = indexStr.length();
			String oleName = enName.substring(0, enName.length() - indexLength);
			Integer index = NumberUtils.toInt(indexStr) + 1;
			enName = oleName + index;
			while (enName.length() > 20) {
				enName = oleName.substring(0, oleName.length() - 1) + index;
			}
		} else {
			if (enName.length() == 20) {
				enName = enName.substring(0, 19) + "2";
			} else {
				enName = enName + "2";
			}
		}
		return copyFactEnName(enName, projectUuid);
	}

	/**
	 * @auto: yinrj
	 * @description: 导入自定义.java或.class或.jar格式模型
	 * @date: 2020/6/8 16:49
	 */
	@Override
	@Transactional
	public Result importJavaFile(String projectUuid, MultipartFile multifile) {
		if (StringUtils.isBlank(projectUuid)) {
			throw new IllegalArgumentException("所属项目uuid为空");
		}
		if (multifile == null) {
			throw new IllegalArgumentException("导入java模型文件不能为空！");
		}
		String filename = multifile.getOriginalFilename();
		logger.info("导入java模型文件：[{}]", filename);

		//转成File文件保存到临时目录
		final String tmpFilePath = resourceResolver.getModelClassPath();
		String path;
		if (tmpFilePath.endsWith(File.separator)) {
			path = tmpFilePath + filename;
		} else {
			path = tmpFilePath + File.separator + filename;
		}
		//校验文件路径
		if (!ValidateUtils.isValidateFileName(path)) {
			throw new IllegalArgumentException("文件不合法");
		}
		File file = SafelyFiles.newFile(path);
		try {
			FileUtils.writeByteArrayToFile(file, multifile.getBytes());
		} catch (IOException e) {
			logger.error("上传模型源文件临时保存本地失败！");
			logger.error(e.getLocalizedMessage(), e);
			throw new IllegalStateException("上传模型源文件失败！");
		}

		boolean javaFile = filename.endsWith(".java");
		boolean classFile = filename.endsWith(".class");
		boolean jarFile = filename.endsWith(".jar");
		if (!(jarFile || classFile || javaFile)) {
			throw new IllegalArgumentException("文件格式不合法");
		}
		List<Assets> assetsList = new ArrayList<>();
		if (javaFile) {
			assetsList = fromJavaFile(file, projectUuid);
		}
		if (classFile) {
			assetsList = fromClassFile(file, projectUuid, filename);
		}
		if (jarFile) {
			assetsList = fromJarFile(file, projectUuid);
		}
		List<String> names = new ArrayList<>();
		List<String> enNames = new ArrayList<>();
		if (CollectionUtils.isEmpty(assetsList)) {
			return Result.newError().withMsg("导入的文件未解析出数据模型, 请确认文件内容");
		}

		for (Assets assets : assetsList) {
			String assetsName = assets.getName();
			if (names.contains(assetsName)) {
				assets.setName(copyName(assetsName, names, 20));
			} else {
				names.add(assetsName);
			}
			String enName = assets.getEnName();
			if (enNames.contains(enName)) {
				assets.setEnName(copyName(enName, enNames, 50));
			} else {
				enNames.add(enName);
			}
		}
		assetsMapper.insertBatch(assetsList);
		Set<Refer> allRefers = new HashSet<>();
		for (Assets assets : assetsList) {
			Set<Refer> refers = getRefers(assets);
			if (CollectionUtils.isNotEmpty(refers)) {
				allRefers.addAll(refers);
			}
		}
		if (CollectionUtils.isNotEmpty(allRefers)) {
			referMapper.insertBatch(allRefers);
		}
		List<Assets> result = new ArrayList<>();
		for (Assets assets : assetsList) {
			Assets resAssets = new Assets();
			resAssets.setUuid(assets.getUuid());
			resAssets.setName(assets.getName());
			resAssets.setType(AssetsType.FACT);
			result.add(resAssets);
		}
		return Result.newSuccess().withData(result);
	}

	/**
	 * 描述: jar 文件转数据模型
	 * @param: [file, projectUuid, filename]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.Assets>
	 */
	private List<Assets> fromJarFile(File file, String projectUuid) {
		try {
			List<Assets> assetsList = new ArrayList<>();
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			String tmpFilePath = resourceResolver.getModelClassPath();
			Map<String, String> nameUuidMap = new HashMap<>();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				if (!entry.isDirectory() && entry.getName().equals(".class")) {
					String orignName = entry.getName();
					String entryName = orignName.substring(orignName.lastIndexOf("/") + 1);
					File fileEntry = null;
					try (InputStream in = jarFile.getInputStream(entry)) {
						String path;
						if (tmpFilePath.equals(File.separator)) {
							path = tmpFilePath + entryName;
						} else {
							path = tmpFilePath + File.separator + entryName;
						}
						fileEntry = SafelyFiles.newFile(path);
						FileUtils.copyInputStreamToFile(in, fileEntry);
					} catch (Exception e) {
						logger.warn("文件解析失败： fileName: {}", entryName);
						continue;
					}
					if (fileEntry != null) {
						try {
							String modelName = entryName.substring(0, entryName.length() - 6);
							String assetsUuid = nameUuidMap.get(modelName);
							if (assetsUuid == null) {
								assetsUuid = IdUtils.UUID();
							}
							String modelJson = loadModelClassToJson(tmpFilePath, entryName, nameUuidMap);
							if (StringUtils.isBlank(modelJson)) {
								logger.warn("无字段：{}", entryName);
								continue;
							}
							nameUuidMap.put(modelName, assetsUuid);
							Assets assets = createFact(projectUuid, modelJson, modelName, assetsUuid);
							assetsList.add(assets);
							logger.info("解析成功：fileName ：{}", entryName);
						} catch (Exception e) {
							logger.warn("文件解析失败： fileName: {}", entryName);
						}
					}
				}
			}
			assetsList = getEmptyFact(nameUuidMap, assetsList, projectUuid);
			return assetsList;
		} catch (Exception e) {
			logger.error("文件解析失败: file: {}", file.getAbsolutePath(), e);
			throw new IllegalStateException("上传模型文件失败");
		}
	}

	private List<Assets> fromClassFile(File file, String projectUuid, String fileName) {
		try {
			List<Assets> assetsList = new ArrayList<>();
			Map<String, String> nameUuidMap = new HashMap<>();
			String modelJson = loadModelClassToJson(resourceResolver.getModelClassPath(), fileName, nameUuidMap);
			String assetsUuid = IdUtils.UUID();
			String modelName = fileName.substring(0, fileName.length() - 6);
			Assets assets = createFact(projectUuid, modelJson, modelName, assetsUuid);
			assetsList.add(assets);
			assetsList = getEmptyFact(nameUuidMap, assetsList, projectUuid);
			return assetsList;
		} catch (Exception e) {
			logger.error("文件解析失败: file: {}", file.getAbsolutePath(), e);
			throw new IllegalStateException("上传模型文件失败");
		}
	}

	private List<Assets> fromJavaFile(File file, String projectUuid) {
		JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
		int res = javac.run(null, null, null, file.getAbsolutePath());
		if (res != 0) {
			throw new IllegalArgumentException("java文件编译失败，请确认文件内容");
		}
		try {
			List<Assets> assetsList = new ArrayList<>();
			Map<String, String> nameUuidMap = new HashMap<>();
			String fileName = file.getName().replace(".java", ".class");
			String modelJson = loadModelClassToJson(resourceResolver.getModelClassPath(), fileName, nameUuidMap);
			String assetsUuid = IdUtils.UUID();
			String modelName = fileName.substring(0, fileName.length() - 6);
			Assets assets = createFact(projectUuid, modelJson, modelName, assetsUuid);
			assetsList.add(assets);
			assetsList = getEmptyFact(nameUuidMap, assetsList, projectUuid);
			return assetsList;
		} catch (Exception e) {
			logger.error("文件解析失败: file: {}", file.getAbsolutePath(), e);
			throw new IllegalStateException("上传模型文件失败");
		}
	}

	/**
	 * 描述: 导入数据模型时引用的空的数据模型也创建
	 * @param: [nameUuidMap, assetsList, projectUuid]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.Assets>
	 */
	private List<Assets> getEmptyFact(Map<String, String> nameUuidMap, List<Assets> assetsList, String projectUuid) {
		List<Assets> emptyAssetsList = new ArrayList<>();
		for (Map.Entry<String, String> entry : nameUuidMap.entrySet()) {
			boolean flag = true;
			String uuid = entry.getValue();
			for (Assets assets : assetsList) {
				if (uuid.equals(assets.getUuid())) {
					flag = false;
					break;
				}
			}
			if (flag) {
				Assets emptyFact = createFact(projectUuid, "{\"fields\":[]}", entry.getKey(), entry.getValue());
				emptyAssetsList.add(emptyFact);
			}
		}
		if (CollectionUtils.isNotEmpty(emptyAssetsList)) {
			assetsList.addAll(emptyAssetsList);
		}
		return assetsList;
	}


	/**
	 * 描述: 根据关键词查询变量或者规则
	 * @param: [projectUuid, keyword, type]
	 * @author: 周庚新
	 * @date: 2020/6/18
	 * @return: com.beagledata.common.Result
	 */
	@Override
	public Result searchVariablesOrRules(String projectUuid, String keyword, String type) {

		if (StringUtils.isBlank(projectUuid) || StringUtils.isBlank(keyword) || StringUtils.isBlank(type)) {
			throw new IllegalArgumentException("参数缺失！");
		}
		if ("rule".equals(type)) {
			List<String> ruleTypes = Arrays.asList(AssetsType.RULE_TABLE, AssetsType.GUIDED_RULE, AssetsType.RULE_TREE, AssetsType.SCORECARD, AssetsType.RULE_FLOW, AssetsType.RULE_SCRIPT);
			List<Assets> ruleAssets = assetsMapper.selectAssetsByProjectUuid(projectUuid, ruleTypes);
			if (CollectionUtils.isEmpty(ruleAssets)) {
				return Result.emptyList();
			}
			List<Assets> res = getRules(ruleAssets, keyword);
			return Result.newSuccess().withData(res);
		} else if ("variable".equals(type)) {
			List<String> variableTypes = Arrays.asList(AssetsType.FACT, AssetsType.CONSTANT);
			List<Assets> assetsList = assetsMapper.selectAssetsByProjectUuid(projectUuid, variableTypes);
			List<String> ruleTypes = Arrays.asList(AssetsType.RULE_TABLE, AssetsType.GUIDED_RULE, AssetsType.RULE_TREE, AssetsType.SCORECARD, AssetsType.RULE_FLOW);
			List<Assets> ruleAssets = assetsMapper.selectAssetsByProjectUuid(projectUuid, ruleTypes);
			if (CollectionUtils.isEmpty(assetsList) && CollectionUtils.isEmpty(ruleAssets)) {
				return Result.emptyList();
			}
			Set<Assets> res = getVariablesAndRules(ruleAssets, assetsList, keyword);
			return Result.newSuccess().withData(res);
		} else {
			throw new IllegalArgumentException("查询类型有误");
		}

	}

	/**
	 * 描述: 根据节点类型和父节点uuid 获取数的子节点
	 * @param: [projectUuid, parentUuid, type]
	 * @author: 周庚新
	 * @date: 2020/6/24
	 * @return: com.beagledata.common.Result
	 */
	@Override
	public Result getAssetsTreeNode(String projectUuid, String parentUuid, String type) {
		if (StringUtils.isBlank(projectUuid) || StringUtils.isBlank(type)) {
			throw new IllegalArgumentException("请求参数缺失！");
		}
		if (StringUtils.isBlank(parentUuid)) {
			parentUuid = "0";
		}

		try {
			if (!type.startsWith("tpl_")) {
				return Result.newSuccess().withData(assetsMapper.getAssetsTreeNode(projectUuid, parentUuid, type));
			}
			return Result.newSuccess().withData(
					assetsTemplateMapper.getAssetsTreeNode(projectUuid, type.replace("tpl_", ""))
			);
		} catch (Exception e) {
			logger.error("查询资源树节点失败. projectUuid: {}, parentUuid: {}, type: {}", projectUuid, parentUuid, type, e);
		}
		return Result.emptyList();
	}

	/**
	 * 描述: 获取某一个分类下边的 文件夹树 （只有文件夹）
	 * @param: [projectUuid, type]
	 * @author: 周庚新
	 * @date: 2020/6/24
	 * @return: com.beagledata.common.Result
	 */
	@Override
	public Result getFolderTree(String projectUuid, String type) {
		Folder folder = new Folder();
		folder.setProjectUuid(projectUuid);
		folder.setCategoryName(type);
		List<Folder> folders = folderMapper.selectByParames(folder);
		JSONArray jsonArray = listToTree(folders, "uuid", "pUuid", "children", type);
		JSONArray result = new JSONArray();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject object = jsonArray.getJSONObject(i);
			if ("0".equals(object.getString("pUuid"))) {
				result.add(object);
			}
		}
		return Result.newSuccess().withData(result);
	}

	/**
	 * 描述: 规则校验
	 * @param: [verifierVO]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: com.beagledata.common.Result
	 */
	@Override
	public Result verifier(VerifierVO verifierVO) {
		try {
			Assets assets = null;
			if (verifierVO.getVersionNo() == null || verifierVO.getVersionNo() < 1) {
				assets = assetsMapper.selectByUuid(verifierVO.getUuid());
			} else {
				assets = assetsMapper.selectVersion(verifierVO.getUuid(), verifierVO.getVersionNo());
			}

			if (assets == null) {
				throw new IllegalArgumentException("资源文件不存在");
			}

			if (StringUtils.isBlank(assets.getContent())) {
				return Result.newSuccess().withData(Collections.emptyList());
			}

			Parser parser = ParserFactory.getParser(assets);
			if (!(parser instanceof DrlParser)) {
				throw new IllegalArgumentException("当前资源文件不支持校验");
			}
			DrlParser drlParser = (DrlParser) parser;
			drlParser.setForVerifier(true);
			Drl drl = ((DrlParser) parser).getDumper();
			RuleVerifier ruleVerifier = new RuleVerifierImpl();
			String ruleContent = drl.dump();
			List<VerifierResultVO> results = ruleVerifier.verifier(ruleContent, verifierVO.getTypes());
			return Result.newSuccess().withData(results);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return Result.newError().withMsg("当前规则自定义属性较多,暂不提供辅助校验");
		}

	}

	/**
	 * 描述: 规则导出
	 * @param: [uuid, response]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: void
	 */
	@Override
	public void export(String uuid, HttpServletResponse response) {
		if (StringUtils.isEmpty(uuid)) {
			throw new IllegalArgumentException("参数缺失");
		}

		Assets assets = assetsMapper.selectByUuid(uuid);
		if (assets == null) {
			throw new IllegalArgumentException("资源文件不存在");
		}

		try {
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(assets.getName().getBytes(), "ISO-8859-1") + ".rule");
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ZipOutputStream zip = new ZipOutputStream(outputStream);
			String projectUuid = assets.getProjectUuid();
			zipBaseData(zip, uuid, projectUuid);
//			zipAssetsUuid(uuid, zip);
			Set<String> uuids = zipAssets(assets, zip);
			zipAssetsVersion(uuids, zip);
			zipAssetsFunction(uuids, zip);
			zipAssetsAiModel(uuids, zip);
			zip.closeEntry();
			zip.flush();
			byte[] bytes = PackageUtils.encrypt(outputStream.toByteArray());
			OutputStream os = response.getOutputStream();
			os.write(bytes);
			os.flush();
			os.close();
		} catch (Exception e) {
			logger.error("资源文件下载失败：{}", uuid, e);
			throw new IllegalStateException("导出失败");
		}

	}


	/**
	 * 描述: 写出一个基础信息文件，用作导入识别，本项目导出的规则再次导入本项目时不导入
	 * @param: [zip]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: void
	 */
	private void zipBaseData(ZipOutputStream zip, String uuid, String projectUuid) throws IOException {
		zip.putNextEntry(new ZipEntry("baseData"));
		JSONObject baseData = new JSONObject();
		baseData.put("version", "1.1");
		baseData.put("uuid", uuid);
		baseData.put("projectUuid", projectUuid);
		zip.write(baseData.toJSONString().getBytes());
	}


	/**
	 * 描述: 写出所有的资源文件
	 * @param: [assets, zip]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: java.util.Set<java.lang.String>
	 */
	private Set<String> zipAssets(Assets assets, ZipOutputStream zip) throws IOException {
		Set<String> assetsUuids = new HashSet<>();
		assetsUuids.add(assets.getUuid());
		//最终导出的资源uuid集合
		Set<String> exportUuids = new HashSet<>();
		getAllReferAssets(assetsUuids, exportUuids);
		List<Assets> exportAssetsList = assetsMapper.selectByUuids(exportUuids);
		if (CollectionUtils.isEmpty(exportAssetsList)) {
			return exportUuids;
		}
		List<Assets> finalExport = exportAssetsList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Assets::getUuid))), ArrayList::new));
		String lines = finalExport.stream().map(line -> JSON.toJSONString(line)).collect(Collectors.joining("\n"));
		zip.putNextEntry(new ZipEntry("assets"));
		zip.write(lines.getBytes());
		return exportUuids;
	}

	/**
	 * 描述: 写出所有资源版本
	 * @param: [uuids, zip]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: void
	 */
	private void zipAssetsVersion(Set<String> uuids, ZipOutputStream zip) throws IOException {
		if (CollectionUtils.isEmpty(uuids)) {
			return;
		}
		List<AssetsVersion> assetsVersions = assetsMapper.selectVersionsByUuids(uuids);
		String lines = assetsVersions.stream().map(assetsVersion -> JSON.toJSONString(assetsVersion)).collect(Collectors.joining("\n"));
		zip.putNextEntry(new ZipEntry("assetsVersions"));
		zip.write(lines.getBytes());
	}

	/**
	 * 描述: 写出所有引用的方法
	 * @param: [uuids, zip]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: void
	 */
	private void zipAssetsFunction(Set<String> uuids, ZipOutputStream zip) throws IOException {
		if (CollectionUtils.isEmpty(uuids)) {
			return;
		}
		List<FunctionDefinition> functionDefinitions = referMapper.selectFunctionByReferUuids(uuids);
		String lines = functionDefinitions.stream().map(line -> JSON.toJSONString(line)).collect(Collectors.joining("\n"));
		zip.putNextEntry(new ZipEntry("functionDefinitions"));
		zip.write(lines.getBytes());
	}

	/**
	 * 描述: 写出所有的aimodel
	 * @param: [uuids, zip]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: void
	 */
	private void zipAssetsAiModel(Set<String> uuids, ZipOutputStream zip) throws IOException {
		if (CollectionUtils.isEmpty(uuids)) {
			return;
		}
		Set<AiModel> models = referMapper.selectAiModelByReferUuid(uuids);
		if (CollectionUtils.isNotEmpty(models)) {
			Iterator<AiModel> it = models.iterator();
			while (it.hasNext()) {
				AiModel aiModel = it.next();
				File modelFile = SafelyFiles.newFile(resourceResolver.getModelPath(), aiModel.getJarName());
				if (modelFile.exists()) {
					zip.putNextEntry(new ZipEntry(aiModel.getJarName()));
					zip.write(FileUtils.readFileToByteArray(modelFile));
				} else {
					it.remove();
				}
			}
			if (CollectionUtils.isNotEmpty(models)) {
				String lines = models.stream().map(model -> JSON.toJSONString(model)).collect(Collectors.joining("\n"));
				zip.putNextEntry(new ZipEntry("models"));
				zip.write(lines.getBytes());
			}
		}
	}

	/**
	 * 描述: 递归获取引用
	 * @param: [referUuids, exportUuids]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: void
	 */
	private void getAllReferAssets(Set<String> referUuids, Set<String> exportUuids) {
		exportUuids.addAll(referUuids);
		List<String> uuids = referMapper.selectSubjectUuidByReferUuid(referUuids);
		if (CollectionUtils.isNotEmpty(uuids) && !exportUuids.containsAll(uuids)) {
			getAllReferAssets(new HashSet<>(uuids), exportUuids);
		}
	}

	/**
	 * 描述: 规则导入
	 * @param: [projectUuid, file]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: com.beagledata.common.Result
	 */
	@Override
	@Transactional
	public Result importRule(String projectUuid, MultipartFile file) {
		if (StringUtils.isEmpty(projectUuid) || file == null || file.isEmpty()) {
			throw new IllegalArgumentException("参数缺失");
		}
		ZipEntry zipEntry;
		//基础信息，用作导入识别
		String baseData = null;
		//导入规则的原始uuid
		String assetsUuid = null;
		//AiModel
		List<String> modelLines = null;
		//AiModel 文件byte
		Map<String, byte[]> modelFiles = new HashMap<>();
		//资源文件
		List<String> assetslLines = null;
		//资源版本文件
		List<String> assetsVersionlLines = null;
		//方法
		List<String> funcLines = null;

		ZipInputStream zis = null;

		try {
			byte[] bytes = PackageUtils.decrypt(file.getBytes());
			zis = new ZipInputStream(new ByteArrayInputStream(bytes));
			while ((zipEntry = zis.getNextEntry()) != null) {
				String entryName = zipEntry.getName();
				if ("baseData".equals(entryName)) {
					baseData = IOUtils.toString(zis);
					if (StringUtils.isBlank(baseData)) {
						throw new IllegalArgumentException("当前文件有误，请下载新的文件导入");
					}
					JSONObject baseDataObj = JSONObject.parseObject(baseData);
					String version = baseDataObj.getString("version");
					assetsUuid = baseDataObj.getString("uuid");
					String oldProjectUuid = baseDataObj.getString("projectUuid");
					if (!"1.1".equals(version) || StringUtils.isBlank(assetsUuid) || StringUtils.isBlank(oldProjectUuid)) {
						throw new IllegalArgumentException("当前文件有误，请下载新的文件导入");
					}
					if (oldProjectUuid.equals(projectUuid)) {
						Assets assets = assetsMapper.selectByUuid(assetsUuid);
						if (assets != null) {
							throw new IllegalArgumentException("请不要将本项目导出的资源文件再次导入本项目！");
						}
					}
				} else if ("assets".equals(entryName)) {
					assetslLines = IOUtils.readLines(zis);
				} else if ("assetsVersions".equals(entryName)) {
					assetsVersionlLines = IOUtils.readLines(zis);
				} else if ("functionDefinitions".equals(entryName)) {
					funcLines = IOUtils.readLines(zis);
				} else if ("models".equals(entryName)) {
					modelLines = IOUtils.readLines(zis);
				} else if (entryName.endsWith(".jar") || entryName.endsWith(".mxl") || entryName.endsWith(".pmml")) {
					modelFiles.put(entryName, IOUtils.toByteArray(zis));
				}
			}

			//数据模型，常量文件
			List<Assets> factAssets = new ArrayList<>();
			List<Assets> updateFactAssets = new ArrayList<>();
			//规则文件
			List<Assets> ruleAssets = new ArrayList<>();
			//资源文件uuid映射
			Map<String, String> uuidMappings = new HashMap<>();
			//数据模型，常量 字段id映射 oldUuid - oldFieldId - newFieldID
//			Map<String, String> fieldMappings = new HashMap<>();
			Map<String, Map<String, String>> fieldMappings = new HashMap<>();
			//资源文件 uuid 类型映射
			Map<String, String> typeMappings = new HashMap<>();
			//资源文件 uuid 名称映射
			Map<String, String> ruleNameMappings = new HashMap<>();
			//数据模型，常量 id映射
			Map<Integer, Integer> idMappings = new HashMap<>();
			// 有子类型的的字段id 与 subType 映射
			Map<String, String> subTypeMappings = new HashMap<>();
			parserAssetsLines(projectUuid, assetslLines, uuidMappings, typeMappings, ruleNameMappings, fieldMappings, idMappings, factAssets, ruleAssets, updateFactAssets, subTypeMappings);

			//插入模型文件返回新旧模型uuid映射
			Map<String, String> modelMappings = importModels(modelLines, modelFiles);
			//插入方法，返回方法名称映射
			Map<String, String> funNameMap = importFunc(funcLines);
			//插入数据模型和常量，并返回新旧id映射

			Map<Integer, Integer> insertIdPapping = importFact(factAssets, uuidMappings, fieldMappings, funNameMap, subTypeMappings);
			idMappings.putAll(insertIdPapping);
			updateFact(updateFactAssets, uuidMappings, fieldMappings, funNameMap, subTypeMappings);
			//插入资源文件
			importRuleAssets(projectUuid, ruleAssets, uuidMappings, fieldMappings, idMappings, ruleNameMappings, modelMappings, funNameMap, subTypeMappings);
			List<AssetsVersion> assetsVersions = importAssetsVersion(assetsVersionlLines, uuidMappings, fieldMappings, idMappings, ruleNameMappings, typeMappings, modelMappings, funNameMap, subTypeMappings);
			Set<Refer> refers = parserRefers(factAssets, ruleAssets, assetsVersions);
			if (CollectionUtils.isNotEmpty(refers)) {
				referMapper.insertBatch(refers);
			}
			JSONObject resObj = new JSONObject();
			List<Assets> finalAsset = new ArrayList<>();
			finalAsset.addAll(factAssets);
			finalAsset.addAll(ruleAssets);
			JSONArray treeArr = new JSONArray();
			finalAsset.forEach(assets -> {
				JSONObject object = new JSONObject();
				object.put("dirParentId", assets.getType());
				object.put("leaf", true);
				object.put("name", assets.getName());
				object.put("type", assets.getType());
				object.put("uuid", assets.getUuid());
				treeArr.add(object);
			});
			resObj.put("uuid", uuidMappings.get(assetsUuid));
			resObj.put("tree", treeArr);
			return Result.newSuccess().withData(resObj);
		} catch (IllegalArgumentException | IllegalStateException e) {
			if (e.getLocalizedMessage().contains("Illegal base64")) {
				throw new IllegalArgumentException("当前文件有误，请重新下载并导入");
			}
			throw e;
		} catch (Exception e) {
			logger.error("导入规则失败", e);
			throw new IllegalStateException("导入失败");
		} finally {
			IOUtils.closeQuietly(zis);
		}
	}

	/**
	 * Description: 导入规则时需要修改的数据模型
	 * @param updateFactAssets:
	 * @param uuidMappings:
	 * @param fieldMappings:
	 * @param funNameMap:
	 * @return: void
	 * @author: ZhouGengxin
	 * @date: 2021/1/21
	 */
	private void updateFact(List<Assets> updateFactAssets, Map<String, String> uuidMappings, Map<String, Map<String, String>> fieldMappings,
							Map<String, String> funNameMap, Map<String, String> subTypeMappings) {
		if (CollectionUtils.isEmpty(updateFactAssets)) {
			return;
		}
		updateFactAssets.forEach(assets -> {
			String content = assets.getContent();
			if (StringUtils.isNotBlank(content) && AssetsType.FACT.equals(assets.getType())) {
				//替换资源文件uuid
				List<String> deriveDatas = (ArrayList<String>) JSONPath.read(content, "$..deriveData");
				deriveDatas = deriveDatas.stream().filter(deriveData -> StringUtils.isNotBlank(deriveData)).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(deriveDatas)) {
					JSONObject object = JSON.parseObject(content);
					JSONArray array = object.getJSONArray("fields");
					for (int i = 0; i < array.size(); i++) {
						JSONObject fieldObj = array.getJSONObject(i);
						String type = fieldObj.getString("type");
						if ("Derive".equals(type)) {
							String deriveData = fieldObj.getString("deriveData");
							if (StringUtils.isNotBlank(deriveData)) {
								deriveData = replaceFiledId(deriveData, fieldMappings, subTypeMappings);
								fieldObj.put("deriveData", deriveData);
							}
						}
					}
					content = object.toJSONString();
				}
				for (Map.Entry<String, String> entry : uuidMappings.entrySet()) {
					content = content.replace(entry.getKey(), entry.getValue());
				}
				//替换方法名称
				for (Map.Entry<String, String> entry : funNameMap.entrySet()) {
					content = content.replace("\"name\":\"" + entry.getKey(), "\"name\":\"" + entry.getValue());
				}
				assets.setContent(content);
			}
			assetsMapper.update(assets);
		});
	}

	/**
	 * 描述: 导入模型文件
	 * @param: [modelLines, modelFiles]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: java.util.Map<java.lang.String, java.lang.String>
	 */
	private Map<String, String> importModels(List<String> modelLines, Map<String, byte[]> modelFiles) throws IOException {
		Map<String, String> uuidMappings = new HashMap<>();
		if (CollectionUtils.isEmpty(modelLines)) {
			return uuidMappings;
		}
		for (String line : modelLines) {
			AiModel aiModel = JSON.parseObject(line, AiModel.class);
			List<AiModel> existsModels = aiModelMapper.selectByModelNameOrJarName(aiModel.getModelName(), aiModel.getJarName());
			if (CollectionUtils.isNotEmpty(existsModels)) {
				uuidMappings.put(aiModel.getUuid(), existsModels.get(0).getUuid());
			} else {
				byte[] bytes = modelFiles.get(aiModel.getJarName());
				if (bytes == null) {
					continue;
				}
				File modelFile = SafelyFiles.newFile(resourceResolver.getModelPath(), aiModel.getJarName());
				FileUtils.writeByteArrayToFile(modelFile, bytes);
				String newUuid = IdUtils.UUID();
				uuidMappings.put(aiModel.getUuid(), newUuid);
				aiModel.setUuid(newUuid);
				if (aiModel.getJarName() != null &&
						(aiModel.getJarName().endsWith(".jar") || aiModel.getJarName().endsWith(".pmml") )) {
					aiModel.setEnable(true);
				} else {
					aiModel.setEnable(false);
				}
				aiModelMapper.insert(aiModel);
			}
		}
		return uuidMappings;
	}

	/**
	 * 描述: 导入方法
	 * @param: [funcLines]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: java.util.Map<java.lang.String, java.lang.String>
	 */
	private Map<String, String> importFunc(List<String> funcLines) {
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
					if (StringUtils.isNotBlank(content) && content.equals(fd.getContent())) {
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

	/**
	 * 描述: 复制方法名称和类名称，防止重复
	 * @param: [func]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: void
	 */
	private void copyFunctionName(FunctionDefinition func) {
		List<FunctionDefinition> existsFunc = functionDefinitionMapper.selectByNameOrClassName(func.getName(), func.getClassName());
		if (!existsFunc.isEmpty()) {
			String name = func.getName();
			String className = func.getClassName();
			Matcher matcher = namePattern.matcher(name);
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

			matcher = namePattern.matcher(className);
			if (matcher.matches()) {
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
	 * 描述: 导入数据模型，常量
	 * @param: [factAssets, uuidMappings, fieldMappings, funNameMap]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: java.util.Map<java.lang.Integer, java.lang.Integer>
	 */
	private Map<Integer, Integer> importFact(List<Assets> factAssets, Map<String, String> uuidMappings, Map<String, Map<String, String>> fieldMappings,
											 Map<String, String> funNameMap, Map<String, String> subTypeMappings) {
		Map<Integer, Integer> idMappings = new HashMap<>();
		if (CollectionUtils.isEmpty(factAssets)) {
			return idMappings;
		}
		factAssets.forEach(assets -> {
			String content = assets.getContent();
			Integer oldId = assets.getId();
			if (StringUtils.isNotBlank(content) && AssetsType.FACT.equals(assets.getType())) {
				List<String> deriveDatas = (ArrayList<String>) JSONPath.read(content, "$..deriveData");
				deriveDatas = deriveDatas.stream().filter(deriveData -> StringUtils.isNotBlank(deriveData)).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(deriveDatas)) {
					JSONObject object = JSON.parseObject(content);
					JSONArray array = object.getJSONArray("fields");
					for (int i = 0; i < array.size(); i++) {
						JSONObject fieldObj = array.getJSONObject(i);
						String type = fieldObj.getString("type");
						if ("Derive".equals(type)) {
							String deriveData = fieldObj.getString("deriveData");
							if (StringUtils.isNotBlank(deriveData)) {
								deriveData = replaceFiledId(deriveData, fieldMappings, subTypeMappings);
								fieldObj.put("deriveData", deriveData);
							}
						}
					}
					content = object.toJSONString();
				}
				//替换资源文件uuid
				for (Map.Entry<String, String> entry : uuidMappings.entrySet()) {
					content = content.replace(entry.getKey(), entry.getValue());
				}
				//替换方法名称
				for (Map.Entry<String, String> entry : funNameMap.entrySet()) {
					content = content.replace("\"name\":\"" + entry.getKey(), "\"name\":\"" + entry.getValue());
				}
				assets.setContent(content);
			}
			assetsMapper.insert(assets);
			idMappings.put(oldId, assets.getId());
		});
		return idMappings;
	}

	/**
	 * 描述: 导入资源文件
	 * @param: [projectUuid, ruleAssets, uuidMappings, fieldMappings, ruleNameMappings, modelMappings, funNameMap]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: void
	 */
	private void importRuleAssets(String projectUuid, List<Assets> ruleAssets, Map<String, String> uuidMappings,
								  Map<String, Map<String, String>> fieldMappings, Map<Integer, Integer> idMappings,
								  Map<String, String> ruleNameMappings, Map<String, String> modelMappings, Map<String, String> funNameMap, Map<String, String> subTypeMappings) {
		if (CollectionUtils.isEmpty(ruleAssets)) {
			return;
		}
		ruleAssets.forEach(assets -> {
			String content = assets.getContent();
			if (StringUtils.isNotBlank(content)) {
				String type = assets.getType();
				content = replaceContent(type, content, uuidMappings, fieldMappings, idMappings, modelMappings, funNameMap, ruleNameMappings, subTypeMappings);
				assets.setContent(content);
			}
		});
		assetsMapper.insertBatch(ruleAssets);
	}

	/**
	 * 描述: 导入资源文件版本
	 * @param: [assetsVersionlLines, uuidMappings, fieldMappings, idMappings, typeMappings, modelMappings, funNameMap]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.AssetsVersion>
	 */
	private List<AssetsVersion> importAssetsVersion(List<String> assetsVersionlLines, Map<String, String> uuidMappings,
													Map<String, Map<String, String>> fieldMappings, Map<Integer, Integer> idMappings,
													Map<String, String> ruleNameMappings, Map<String, String> typeMappings,
													Map<String, String> modelMappings, Map<String, String> funNameMap, Map<String, String> subTypeMappings) {
		if (CollectionUtils.isEmpty(assetsVersionlLines)) {
			return null;
		}
		List<AssetsVersion> assetsVersionList = assetsVersionlLines.stream().map(line -> {
			AssetsVersion assetsVersion = JSON.parseObject(line, AssetsVersion.class);
			String assetsUuid = assetsVersion.getAssetsUuid();
			String type = typeMappings.get(assetsUuid);
			assetsVersion.setAssetUuid(uuidMappings.get(assetsUuid));
			assetsVersion.setCreator(UserHolder.currentUser());
			assetsVersion.setType(type);
			String content = assetsVersion.getContent();
			if (StringUtils.isNotBlank(content) && !AssetsType.CONSTANT.equals(type)) {
				content = replaceContent(type, content, uuidMappings, fieldMappings, idMappings, modelMappings, funNameMap, ruleNameMappings, subTypeMappings);
				assetsVersion.setContent(content);
			}
			return assetsVersion;
		}).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(assetsVersionList)) {
			assetsMapper.insertVersionBatch(assetsVersionList);
			return assetsVersionList;
		}
		return null;
	}

	/**
	 * 描述: 导入替换规则文件中的uuid ，id 等内容
	 * @param: [type, content, uuidMappings, fieldMappings, idMappings, modelMappings, funNameMap]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: java.lang.String
	 */
	private String replaceContent(String type, String content, Map<String, String> uuidMappings,
								  Map<String, Map<String, String>> fieldMappings, Map<Integer, Integer> idMappings,
								  Map<String, String> modelMappings, Map<String, String> funNameMap,
								  Map<String, String> ruleNameMapping, Map<String, String> subTypeMappings) {
		if (AssetsType.RULE_SCRIPT.equals(type)) {
			Matcher matcher = scriptFactPattern.matcher(content);
			while (matcher.find()) {
				String factId = matcher.group(1);
				try {
					Integer id = NumberUtils.toInt(factId);
					Integer newId = idMappings.get(id);
					content = content.replace(matcher.group(), "Fact_" + newId);
				} catch (Exception e) {

				}
			}
		} else {
			// 决策流替换规则名称
			if (AssetsType.RULE_FLOW.equals(type)) {
				JSONObject ruleFlow = JSON.parseObject(content);
				JSONArray nodeDataArray = ruleFlow.getJSONArray("nodeDataArray");
				if (CollectionUtils.isNotEmpty(nodeDataArray)) {
					for (int i = 0; i < nodeDataArray.size(); i++) {
						JSONObject nodeData = nodeDataArray.getJSONObject(i);
						String category = nodeData.getString("category");
						if ("Rule".equals(category)) {
							JSONArray ruleArr = nodeData.getJSONArray("rules");
							if (CollectionUtils.isNotEmpty(ruleArr)) {
								for (int j = 0; j < ruleArr.size(); j++) {
									JSONObject rule = ruleArr.getJSONObject(j);
									String uuid = rule.getString("uuid");
									String name = ruleNameMapping.get(uuid);
									rule.put("name", name);
								}
							}
						}
					}
					content = ruleFlow.toJSONString();
				}
			}
			//替换资源文件uuid
			content = replaceFiledId(content, fieldMappings, subTypeMappings);

			for (Map.Entry<String, String> entry : uuidMappings.entrySet()) {
				content = content.replace(entry.getKey(), entry.getValue());
			}
			//替换ai模型uuid
			for (Map.Entry<String, String> entry : modelMappings.entrySet()) {
				content = content.replace(entry.getKey(), entry.getValue());
			}

			//替换方法名称
			for (Map.Entry<String, String> entry : funNameMap.entrySet()) {
				content = content.replace("\"name\":\"" + entry.getKey(), "\"name\":\"" + entry.getValue());
			}
		}
		return content;
	}

	private String replaceFiledId(String data, Map<String, Map<String, String>> fieldIdMappings, Map<String, String> subTypeMappings) {
		JSONObject jsonObject = JSON.parseObject(data);
		Map<String, Object> keyPaths = JSONPath.paths(jsonObject);
		for (Map.Entry<String, Object> entry : keyPaths.entrySet()) {
			String path = entry.getKey();
			if (path.lastIndexOf("/") == 0) {
				continue;
			}
			int index = path.lastIndexOf("/");
			String name = path.substring(index + 1);
			if ("id".equals(name)) {
				String uuid = (String) JSONPath.read(data, path);
				Map<String, String> fieldMap = fieldIdMappings.get(uuid);
				if (fieldMap == null) {
				    continue;
				}
				String fieldIdPath = path.substring(0, index) + "/fieldId";
				String subjectChild = (String) JSONPath.read(data, fieldIdPath);
				if (StringUtils.isNotBlank(subjectChild) && subjectChild.contains(",")) {
					String[] subjectChilds = subjectChild.split(",");
					List<String> fieldIds = new ArrayList<>();
					fieldIds.add(fieldMap.get(subjectChilds[0]));
					String subFiled = subjectChilds[0];
					for (int i = 1; i < subjectChilds.length; i++) {
						String filed = subjectChilds[i];
						String subUuid = subTypeMappings.get(subFiled);
						if (subUuid == null) {
							fieldIds.add(filed);
							continue;
						}
						subFiled = filed;
						Map<String, String> fieldMaps = fieldIdMappings.get(subUuid);
						if (fieldMap == null) {
							fieldIds.add(filed);
							continue;
						}
						fieldIds.add(fieldMaps.get(filed));
					}
					JSONPath.set(jsonObject, fieldIdPath, StringUtils.join(fieldIds, ","));
				} else {
					JSONPath.set(jsonObject, fieldIdPath, fieldMap.get(subjectChild));
				}
				data = jsonObject.toJSONString();
			}
		}
		return data;
	}

	/**
	 * 描述: 解析导入资源的引用关系
	 * @param: [factAssets, ruleAssets, assetsVersions]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: java.util.Set<com.beagledata.gaea.workbench.entity.Refer>
	 */
	private Set<Refer> parserRefers(List<Assets> factAssets, List<Assets> ruleAssets, List<AssetsVersion> assetsVersions) {
		Set<Refer> refers = new HashSet();
		if (CollectionUtils.isNotEmpty(factAssets)) {
			for (Assets assets : factAssets) {
				Set<Refer> factRefer = getRefers(assets);
				if (CollectionUtils.isNotEmpty(factRefer)) {
					refers.addAll(factRefer);
				}
			}
		}
		if (CollectionUtils.isNotEmpty(ruleAssets)) {
			for (Assets assets : ruleAssets) {
				Set<Refer> ruleRefer = getRefers(assets);
				if (CollectionUtils.isNotEmpty(ruleRefer)) {
					refers.addAll(ruleRefer);
				}
			}
		}
		if (CollectionUtils.isNotEmpty(assetsVersions)) {
			for (AssetsVersion assetsVersion : assetsVersions) {
				Assets assets = new Assets();
				assets.setContent(assetsVersion.getContent());
				assets.setVersionNo(assetsVersion.getVersionNo());
				assets.setType(assetsVersion.getType());
				assets.setUuid(assetsVersion.getAssetUuid());
				Set<Refer> versionRefer = getRefers(assets);
				if (CollectionUtils.isNotEmpty(versionRefer)) {
					refers.addAll(versionRefer);
				}
			}
		}
		return refers;
	}


	/**
	 * 解析 资源文件，分为数据模型和贵文件 并映射相关id uuid
	 */
	private void parserAssetsLines(String projectUuid, List<String> assetslLines, Map<String, String> uuidMappings,
								   Map<String, String> typeMappings, Map<String, String> ruleNameMappings,
								   Map<String, Map<String, String>> fieldMappings, Map<Integer, Integer> idMappings,
								   List<Assets> factAssets, List<Assets> ruleAssets, List<Assets> updateFacts, Map<String, String> subTypeMappings) {
		Map<String, List<String>> nameMaps = new HashMap<>();
		List<String> enNames = new ArrayList<>();
		Map<String, Assets> factMaps = new HashMap<>();
		List<Assets> oldRuleAssets = new ArrayList<>();
		List<Assets> oldFactAssets = new ArrayList<>();
		assetslLines.forEach(line -> {
			//数据模型和常量做uuid和文件映射关系，便于查询子类型的文件
			Assets assets = JSON.parseObject(line, Assets.class);
			String type = assets.getType();
			assets.setProjectUuid(projectUuid);
			assets.setCreator(UserHolder.currentUser());
			typeMappings.put(assets.getUuid(), type);
			assets.setDirParentId("0");
			if (AssetsType.FACT.equals(type) || AssetsType.CONSTANT.equals(type)) {
				factMaps.put(assets.getUuid(), assets);
				oldFactAssets.add(assets);
			} else {
				oldRuleAssets.add(assets);
			}
		});

		oldRuleAssets.forEach(assets -> {
			parseRuleAsset(assets, ruleAssets, uuidMappings, ruleNameMappings, nameMaps);
		});
		List<String> hasParse = new ArrayList<>();
		oldFactAssets.forEach(assets -> {
			parseFactAssets(assets, factAssets, updateFacts, uuidMappings, fieldMappings, idMappings, nameMaps, enNames, hasParse, factMaps, subTypeMappings);
		});
	}

	/**
	 * Description: 解析数据模型和常量，获取相应的映射关系
	 * @return: java.lang.Boolean
	 * @author: ZhouGengxin
	 * @date: 2021/1/21
	 */
	private Boolean parseFactAssets(Assets assets, List<Assets> factAssets, List<Assets> updateFacts,
									Map<String, String> uuidMappings, Map<String, Map<String, String>> fieldMappings, Map<Integer, Integer> idMappings,
									Map<String, List<String>> nameMaps, List<String> enNames, List<String> hasParse, Map<String, Assets> factMaps, Map<String, String> subTypeMappings) {
		String uuid = assets.getUuid();
		if (hasParse.contains(uuid)) {
			return true;
		}
		String type = assets.getType();
		Assets updateFact = null;
		Map<String, String> fieldMap = new HashMap<>();
		Boolean needAdd = false;
		if (AssetsType.FACT.equals(type)) {
			String enName = assets.getEnName();
			if (StringUtils.isNotBlank(enName)) {
				Assets existAssets = assetsMapper.selectAssetsByEnName(enName, assets.getProjectUuid());
				if (existAssets == null) {
					needAdd = true;
				} else {
					//导入项目已存在改数据模型，则对字段进行添加修改操作，并返回相应的需要改的数据模型
					updateFact =parseField(assets,existAssets,factAssets,updateFacts,uuidMappings,fieldMap,fieldMappings,idMappings,nameMaps,enNames,hasParse,factMaps,subTypeMappings);
				}
			} else {
				//英文名称为空，根据文件名称查询
				Assets existAssets = assetsMapper.selectFactAssetsByName(assets.getName(), assets.getProjectUuid());
				if (existAssets == null) {
					needAdd = true;
				} else {
					//导入项目已存在改数据模型，则对字段进行添加修改操作，并返回相应的需要改的数据模型
					updateFact =parseField(assets,existAssets,factAssets,updateFacts,uuidMappings,fieldMap,fieldMappings,idMappings,nameMaps,enNames,hasParse,factMaps,subTypeMappings);
				}
			}
		} else {
			Assets existAssets = assetsMapper.selectConstantByNameAndProject(assets.getName(), assets.getProjectUuid());
			if (existAssets == null) {
				needAdd = true;
			} else {
				// 存在则判断 导入的常量的每个常量是否存在 label type value 全部相同 才是存在 ，
				// 存在直接映射id ，不存在新增一个，映射id、
				// label 相同 但是值或type不相同，新增一个，label 加数字判断是否存在
				uuidMappings.put(assets.getUuid(), existAssets.getUuid());
				if (StringUtils.isNotBlank(assets.getContent())) {
					Constant importConstant = Constant.fromAssets(assets);
					Constant constant = Constant.fromAssets(existAssets);
					for (com.beagledata.gaea.workbench.rule.define.Field field : importConstant.getFields()) {
						boolean flag = false;
						List<String> labels = new ArrayList<>();
						do {
							labels.add(field.getLabel());
							flag = existConstantFiled(constant, field, fieldMap);
							if (flag) {
								String newLabel = copyName(field.getLabel(), labels, 20);
								field.setLabel(newLabel);
							}
						} while (flag);
					}
					existAssets.setContent(JSONArray.toJSONString(constant));
				}
				updateFact = existAssets;
				idMappings.put(assets.getId(), existAssets.getId());
			}
		}
		if (needAdd) {
			//需要新增的
			String newUuid = IdUtils.UUID();
			uuidMappings.put(uuid, newUuid);
			assets.setUuid(newUuid);
			List<String> names = nameMaps.get(type);
			String assetsName = copyName(assets.getName(), type, assets.getProjectUuid());
			if (names == null) {
				names = new ArrayList<>();
			} else {
				if (names.contains(assetsName)) {
					assetsName = copyName(assetsName, names, 20);
				}
			}
			assets.setName(assetsName);
			names.add(assetsName);
			nameMaps.put(type, names);
			String enName = assets.getEnName();
			if (StringUtils.isNotBlank(enName)) {
				String newEnName = copyFactEnName(enName, assets.getProjectUuid());
				if (enNames.contains(newEnName)) {
					newEnName = copyName(newEnName, enNames, 50);
				} else {
					enNames.add(newEnName);
				}
				if (!enName.equals(newEnName)) {
					JSONObject object = JSON.parseObject(assets.getContent());
					object.put("enName", newEnName);
					assets.setContent(object.toJSONString());
				}
				assets.setEnName(newEnName);
			}

			String content = assets.getContent();
			content = replaceFactId(content, fieldMap, subTypeMappings);
			assets.setContent(content);
			factAssets.add(assets);
		} else {
			// 需要修改的
			uuidMappings.put(uuid, updateFact.getUuid());
			updateFacts.add(updateFact);

		}
		fieldMappings.put(uuid, fieldMap);
		hasParse.add(uuid);
		return needAdd;
	}

	/**
	 * Description: 如果导入的数据模型已经存在，则比对已有和导入的吗，新的字段添加到已有数据模型中
	 * @return: com.beagledata.gaea.workbench.entity.Assets
	 * @author: ZhouGengxin
	 * @date: 2021/1/21
	 */
	private Assets parseField(Assets assets, Assets existAssets,List<Assets> factAssets, List<Assets> updateFacts,
							Map<String, String> uuidMappings, Map<String, String> fieldMap, Map<String, Map<String, String>> fieldMappings, Map<Integer, Integer> idMappings,
							Map<String, List<String>> nameMaps, List<String> enNames, List<String> hasParse, Map<String, Assets> factMaps, Map<String, String> subTypeMappings){
		// 存在则判断 导入的常量的每个常量是否存在 label type value 全部相同 才是存在 ，
		// 存在直接映射id ，不存在新增一个，映射id、
		// label 相同 但是值或type不相同，新增一个，label 加数字判断是否存在
		uuidMappings.put(assets.getUuid(), existAssets.getUuid());
		if (StringUtils.isNotBlank(assets.getContent())) {
			Fact importFact = Fact.fromAssets(assets);
			Fact fact = Fact.fromAssets(existAssets);
			for (com.beagledata.gaea.workbench.rule.define.Field field : importFact.getFields()) {
				boolean flag = false;
				List<String> labels = new ArrayList<>();
				List<String> names = new ArrayList<>();
				do {
					labels.add(field.getLabel());
					names.add(field.getName());
					String id = field.getId();
					String subType = field.getSubType();
					if (StringUtils.isNotBlank(subType) && subType.length() == 32) {
						subTypeMappings.put(id, subType);
					}
					flag = existFactFiled(fact, field, fieldMap,fieldMappings, factAssets, updateFacts, uuidMappings, idMappings, nameMaps, enNames, hasParse, factMaps, subTypeMappings);
					if (flag) {
						String newLabel = copyName(field.getLabel(), labels, 20);
						String name = copyName(field.getName(), names, 20);
						field.setLabel(newLabel);
						field.setName(name);
					}
				} while (flag);
			}
			existAssets.setContent(JSONArray.toJSONString(fact));
		}
		idMappings.put(assets.getId(), existAssets.getId());
		return existAssets;
	}
	/**
	 * Description: 解析字段是否存在于已有数据模型中，不存在添加
	 * @return: boolean
	 * @author: ZhouGengxin
	 * @date: 2021/1/21
	 */
	private boolean existFactFiled(Fact fact, com.beagledata.gaea.workbench.rule.define.Field field, Map<String, String> fieldMap, Map<String, Map<String, String>> fieldMappings,
								   List<Assets> factAssets, List<Assets> updateFacts,
								   Map<String, String> uuidMappings, Map<Integer, Integer> idMappings,
								   Map<String, List<String>> nameMaps, List<String> enNames, List<String> hasParse, Map<String, Assets> factMaps, Map<String, String> subTypeMappings) {
		String label = field.getLabel();
		com.beagledata.gaea.workbench.rule.define.Field.Type fieldType = field.getType();
		String name = field.getName();
		String subType = field.getSubType();
		String deriveData = field.getDeriveData();
		String id = field.getId();
		PassingDirection.Direction direction = field.getDirection();
		String required = field.getRequired();
		com.beagledata.gaea.workbench.rule.define.Field existField = fact.getFieldByLabelAndName(label, name);
		if (existField != null) {
			if (fieldType.equals(existField.getType())) {
				if (com.beagledata.gaea.workbench.rule.define.Field.Type.Derive.equals(fieldType)) {
					//Derive 衍生字段的格式比较复杂，所以仿作一个新的字段增加
					return true;
				} else if (com.beagledata.gaea.workbench.rule.define.Field.Type.List.equals(fieldType) || com.beagledata.gaea.workbench.rule.define.Field.Type.Set.equals(fieldType)) {
					if (subType != null && subType.length() == 32) {
						parseFactAssets(factMaps.get(subType), factAssets, updateFacts, uuidMappings, fieldMappings, idMappings, nameMaps, enNames, hasParse, factMaps, subTypeMappings);
						if (existField.getSubType().equals(uuidMappings.get(subType))) {
							existField.setDirection(direction);
							existField.setRequired(required);
							fieldMap.put(id, existField.getId());
							return false;
						} else {
							return true;
						}
					} else {
						if (subType.equals(existField.getSubType())) {
							existField.setDirection(direction);
							existField.setRequired(required);
							fieldMap.put(id, existField.getId());
							return false;
						} else {
							return true;
						}
					}
				} else if (com.beagledata.gaea.workbench.rule.define.Field.Type.Map.equals(fieldType)) {
					if (subType != null && subType.length() == 32) {
						parseFactAssets(factMaps.get(subType), factAssets, updateFacts, uuidMappings, fieldMappings, idMappings, nameMaps, enNames, hasParse, factMaps, subTypeMappings);
						if (existField.getSubType().equals(uuidMappings.get(subType))) {
							existField.setDirection(direction);
							existField.setRequired(required);
							fieldMap.put(id, existField.getId());
							return false;
						} else {
							return true;
						}
					} else {
						if (subType.equals(existField.getSubType())) {
							existField.setDirection(direction);
							existField.setRequired(required);
							fieldMap.put(id, existField.getId());
							return false;
						} else {
							return true;
						}
					}
				} else {
					existField.setDirection(direction);
					existField.setRequired(required);
					fieldMap.put(id, existField.getId());
					return false;
				}
			} else {
				return true;
			}
		} else {
			String newId = RandomCode.getSecurityCode(16, RandomCode.SecurityCodeLevel.Hard, true);
			com.beagledata.gaea.workbench.rule.define.Field newField = new com.beagledata.gaea.workbench.rule.define.Field();
			newField.setType(fieldType);
			newField.setLabel(label);
			newField.setId(newId);
			newField.setName(name);
			newField.setSubType(subType);
			newField.setDeriveData(deriveData);
			newField.setDirection(direction);
			newField.setRequired(required);

			fact.getFields().add(newField);
			fieldMap.put(id, newId);
			return false;
		}
	}

	/**
	 * Description: 判断一个field 是否在 Constant中
	 * @param constant: 常量
	 * @param field: 字段
	 * @return: java.lang.Boolean
	 * @author: ZhouGengxin
	 * @date: 2021/1/20
	 */
	private boolean existConstantFiled(Constant constant, com.beagledata.gaea.workbench.rule.define.Field field, Map<String, String> fieldMappings) {
		String label = field.getLabel();
		com.beagledata.gaea.workbench.rule.define.Field.Type fieldType = field.getType();
		String value = field.getValue();
		String id = field.getId();
		com.beagledata.gaea.workbench.rule.define.Field existField = constant.getFieldByLabel(label);
		if (existField != null) {
			if (value.equals(existField.getValue()) && fieldType.equals(existField.getType())) {
				fieldMappings.put(id, existField.getId());
				return false;
			} else {
				return true;
			}
		} else {
			String newId = RandomCode.getSecurityCode(16, RandomCode.SecurityCodeLevel.Hard, true);
			com.beagledata.gaea.workbench.rule.define.Field newField = new com.beagledata.gaea.workbench.rule.define.Field();
			newField.setValue(value);
			newField.setType(fieldType);
			newField.setLabel(label);
			newField.setId(newId);
			constant.getFields().add(newField);
			fieldMappings.put(id, newId);
			return false;
		}
	}

	/**
	 * 描述: 规则文件替换 uuid name 并做好uuid 映射，类型映射 和 规则名称映射
	 * @param: [assets, ruleAssets, uuidMappings, ruleNameMappings, nameMaps]
	 * @author: 周庚新
	 * @date: 2021/1/19
	 * @return: void
	 */
	private void parseRuleAsset(Assets assets, List<Assets> ruleAssets, Map<String, String> uuidMappings, Map<String, String> ruleNameMappings, Map<String, List<String>> nameMaps) {
		// 规则文件替换 uuid name 并做好uuid 映射，类型映射 和 规则名称樱红色
		String newUuid = IdUtils.UUID();
		String oldUuid = assets.getUuid();
		uuidMappings.put(oldUuid, newUuid);
		String type = assets.getType();
		assets.setUuid(newUuid);
		List<String> names = nameMaps.get(type);
		String assetsName = copyName(assets.getName(), type, assets.getProjectUuid());
		if (names == null) {
			names = new ArrayList<>();
		} else {
			if (names.contains(assetsName)) {
				assetsName = copyName(assetsName, names, 20);
			}
		}
		assets.setName(assetsName);
		names.add(assetsName);
		nameMaps.put(type, names);
		ruleNameMappings.put(oldUuid, assets.getName());
		ruleAssets.add(assets);
	}

	@Override
	public Set<Refer> getRefers(Assets assets) {
		Set<Refer> refers = new HashSet<>();
		String content = assets.getContent();
		String type = assets.getType();
		if (StringUtils.isBlank(content)) {
			return refers;
		}
		//脚本式决策集引用
		if (AssetsType.RULE_SCRIPT.equals(type)) {
			return ruleScriptRefer(assets);
		}
		//数据模型引用
		if (AssetsType.FACT.equals(type)) {
			return factRefer(assets);
		}
		//测试案例引用
		if (AssetsType.TEST_CASE.equals(type)) {
			JSONObject fileData = (JSONObject) JSONPath.read(content, "$.fileData");
			if (fileData != null) {
				Refer refer = new Refer(fileData.getString("ruleUuid"), null, null, fileData.getInteger("assetsVersion"), assets.getUuid(), assets.getType(), null);
				refers.add(refer);
			}
			return refers;
		}
		//引用方法
		List<JSONObject> funcs = (List<JSONObject>) JSONPath.read(content, "$..func");
		if (CollectionUtils.isNotEmpty(funcs)) {
			for (int i = 0; i < funcs.size(); i++) {
				JSONObject func = funcs.get(i);
				Refer refer = new Refer(func.getString("name"), "func", func.getString("method"), null, assets.getUuid(), assets.getType(), null);
				refers.add(refer);
			}
		}
		//通用处理
		Map<String, Object> keyPaths = JSONPath.paths(JSON.parseObject(content));
		for (Map.Entry<String, Object> entry : keyPaths.entrySet()) {
			String path = entry.getKey();
			if (path.lastIndexOf("/") == 0) {
				continue;
			}
			int index = path.lastIndexOf("/");
			String name = path.substring(index + 1);
			if ("id".equals(name)) {
				String parentPath = path.substring(0, index);
				JSONObject subjectObject = (JSONObject) JSONPath.read(content, parentPath);
				String subjectType = null;
				if (parentPath.endsWith("model")) {
					subjectType = "model";
				} else if (parentPath.endsWith("thirdApi")) {
					subjectType = "thirdApi";
				}
				String subjectChild = subjectObject.getString("fieldId");
				if (StringUtils.isNotBlank(subjectChild) && subjectChild.contains(",")) {
					String[] subjectChilds = subjectChild.split(",");
					String child = subjectChilds[0];
					String subjectUuid = subjectObject.getString("id");
					Refer refer = new Refer(subjectUuid, null, child, null, assets.getUuid(), assets.getType(), assets.getVersionNo());
					refers.add(refer);

					String searchUuid = new String(subjectUuid);
					for (int i = 0; i < subjectChilds.length - 1; i++) {
						Assets subjectAssets = assetsMapper.selectByUuid(searchUuid);
						Fact fact = Fact.fromAssets(subjectAssets);
						String newSubjectUuid = fact.getFieldById(subjectChilds[i]).getSubType();
						searchUuid = new String(newSubjectUuid);
						String newChild = subjectChilds[i + 1];
						Refer newRefer = new Refer(newSubjectUuid, null, newChild, null, assets.getUuid(), assets.getType(), assets.getVersionNo());
						refers.add(newRefer);
					}

				} else {
					Refer newRefer = new Refer(subjectObject.getString("id"), subjectType, subjectChild, null, assets.getUuid(), assets.getType(), assets.getVersionNo());
					refers.add(newRefer);
				}

			} else if ("uuid".equals(name)) {
				String parentPath = path.substring(0, index);
				JSONObject subjectObject = (JSONObject) JSONPath.read(content, parentPath);
				Refer newRefer = new Refer(subjectObject.getString("uuid"), null, null, subjectObject.getInteger("versionNo"), assets.getUuid(), assets.getType(), assets.getVersionNo());
				refers.add(newRefer);
			}
		}
		return refers;
	}

	@Override
	public Map<String, Integer> countNumByTypeForPrj(String projectUuid, Set<String> types) {
		if (StringUtils.isBlank(projectUuid)) {
			logger.error("统计项目的文件类型数量,项目uuid为空");
			throw new IllegalArgumentException("参数不能为空");
		}
		try {
			Map<String, Map<String, Object>> countMap = assetsMapper.countNumByTypeForPrj(projectUuid);
			if (null == countMap || countMap.size() < 1) {
				logger.info("统计项目的文件类型数量为空");
				return null;
			}

			if (null == types || types.size() < 1) {
				Map map = new HashMap(countMap.size());
				for (Map.Entry<String, Map<String, Object>> entry : countMap.entrySet()) {
					if (null != entry) {
						String type = entry.getKey();
						if (null != entry.getValue()) {
							map.put(type, entry.getValue().get("num"));
						}
					}
				}
				map = countModelForPrj(projectUuid, map);
				return map;
			}

			Map resultMap = new HashMap<>(types.size());
			for (String type : types) {
				Map<String, Object> value = countMap.get(type);
				if (null != value) {
					resultMap.put(type, value.get("num"));
				}
			}
			if (types.contains("model")) {
				resultMap = countModelForPrj(projectUuid, resultMap);
			}
			return resultMap;
		} catch (Exception e) {
			logger.error("统计项目的文件类型数量出错", e);
			throw e;
		}
	}

	@Override
	public Result autoCreateFact(String json) {
		if (StringUtils.isBlank(json)) {
			throw new IllegalArgumentException("请求参数不能为空");
		}
		try {
			JSONObject param = JSON.parseObject(json);
			String projectUuid = param.getString("projectUuid");
			if (StringUtils.isBlank(projectUuid)) {
				throw new IllegalArgumentException("所属项目不能为空");
			}
			Project project = projectMapper.selectByUuid(projectUuid);
			if (null == project) {
				throw new IllegalArgumentException("所属项目不存在");
			}
			String enName = param.getString("enName");
			if (StringUtils.isBlank(enName)) {
				throw new IllegalArgumentException("英文名称(enName)不能为空");
			}
			Assets fact = assetsMapper.selectAssetsByEnName(enName, projectUuid);
			JSONArray fields = param.getJSONArray("fields");
			if (null == fact) {	//新建数据模型
				fact = new Assets();
				fact.setEnName(enName);
				fact.setProjectUuid(projectUuid);
				fact.setType(AssetsType.FACT);
				String factName = param.getString("name");
				if (StringUtils.isBlank(factName)) {
					factName = enName;
				}
				fact.setName(factName);
				fact.setUuid(IdUtils.UUID());
				fact.setDescription(param.getString("description"));
				fact.setContent(getFactContent(fields));
				addAssets(fact);
			} else {	//更新数据模型
				String factName = param.getString("name");
				if (StringUtils.isNotBlank(factName)) {
					fact.setName(factName);
				}
				if (StringUtils.isNotBlank(param.getString("description"))) {
					fact.setDescription(param.getString("description"));
				}
				fact.setContent(getFactContent(fields));
				editAssets(fact);
			}
			return Result.newSuccess();
		} catch (Exception e) {
			logger.error("自动创建数据模型出错, param=[{]", json, e.getLocalizedMessage());
			throw e;
		}
	}

	private String getFactContent(JSONArray fields) {
		if (null == fields || fields.size() < 1) {
			return "";
		}

		//设置字段
		for (int i = 0; i < fields.size(); i++) {
			JSONObject field = fields.getJSONObject(i);
			if (null == field) {
				continue;
			}
			String fieldName = field.getString("name");
			String fieldType = field.getString("type");
			if (StringUtils.isBlank(fieldName) || StringUtils.isBlank(fieldType)) {
				logger.warn("字段创建数据模型, 字段英文名称或字段类型为空, 序号=[{}], 跳过该字段", i);
				continue;
			}
			if (StringUtils.isBlank(field.getString("label"))) {
				field.put("label", fieldName);	//如果字段中文名称为空, 则设置为字段的英文名称
			}
			//补充字段的其他属性
			field.put("id", RandomCode.getSecurityCode(16, RandomCode.SecurityCodeLevel.Hard, true));
			field.put("deriveData", "");
			field.put("direction", PassingDirection.Direction.IN_OUT);
			field.put("subType", "");
		}
		JSONObject content = new JSONObject(1);
		content.put("fields", fields);
		return content.toJSONString();
	}

	/**
	 * 增加项目中模型的数量统计
	 */
	public Map<String, Integer> countModelForPrj(String projectUuid, Map<String, Integer> map) {
		if (null == map) {
			map = new HashMap<>(1);
		}
		Integer total = aiModelMapper.countTotal(null, null, true, null);
		map.put("model", total);
		return map;
	}

	/**
	 * 描述: 数据模型引用
	 * @param: [assets]
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: java.util.Set<com.beagledata.gaea.workbench.entity.Refer>
	 */
	private Set<Refer> factRefer(Assets assets) {
		Set<Refer> refers = new HashSet<>();
		String content = assets.getContent();
		//子类型
		List<String> subTypeUuids = (ArrayList<String>) JSONPath.read(content, "$..subType");
		if (CollectionUtils.isNotEmpty(subTypeUuids)) {
			List<Assets> assetsList = assetsMapper.selectByUuids(subTypeUuids);
			for (Assets subject : assetsList) {
				Refer refer = new Refer(subject.getUuid(), null, null, null, assets.getUuid(), assets.getType(), null);
				refers.add(refer);
			}
		}
		//衍生类型
		List<String> deriveDatas = (ArrayList<String>) JSONPath.read(content, "$..deriveData");
		deriveDatas = deriveDatas.stream().filter(deriveData -> StringUtils.isNotBlank(deriveData)).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(deriveDatas)) {
			for (String deriveData : deriveDatas) {
				List<JSONObject> facts = (List<JSONObject>) JSONPath.read(deriveData, "$..fact");
				//子类型
				if (CollectionUtils.isNotEmpty(facts)) {
					for (int i = 0; i < facts.size(); i++) {
						JSONObject fact = facts.get(i);
						Refer refer = new Refer(fact.getString("id"), null, fact.getString("fieldId"), null, assets.getUuid(), assets.getType(), null);
						refers.add(refer);
					}
				}
				//方法
				List<JSONObject> funcs = (List<JSONObject>) JSONPath.read(deriveData, "$..func");
				if (CollectionUtils.isNotEmpty(funcs)) {
					for (int i = 0; i < funcs.size(); i++) {
						JSONObject func = funcs.get(i);
						Refer refer = new Refer(func.getString("name"), "func", func.getString("method"), null, assets.getUuid(), assets.getType(), null);
						refers.add(refer);
					}
				}
			}
		}
		return refers;
	}

	/**
	 * 描述: 脚本式决策集引用
	 * @param: []
	 * @author: 周庚新
	 * @date: 2020/11/13
	 * @return: java.util.Set<com.beagledata.gaea.workbench.entity.Refer>
	 */
	private Set<Refer> ruleScriptRefer(Assets assets) {
		List<Integer> ids = new ArrayList<>();
		Set<Refer> refers = new HashSet<>();
		String content = assets.getContent();
		Matcher matcher = scriptFactPattern.matcher(content);
		while (matcher.find()) {
			String factId = matcher.group(1);
			try {
				Integer id = NumberUtils.toInt(factId);
				ids.add(id);
			} catch (Exception e) {

			}
		}
		if (CollectionUtils.isEmpty(ids)) {
			return refers;
		}
		List<Assets> assetsList = assetsMapper.selectByIds(ids);
		for (Assets subject : assetsList) {
			Refer refer = new Refer(subject.getUuid(), null, null, null, assets.getUuid(), assets.getType(), assets.getVersionNo());
			refers.add(refer);
		}
		return refers;
	}

	/**
	 * 描述: list 数组转树结构
	 * @param: [folders, id, pid, child, type]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: com.alibaba.fastjson.JSONArray
	 */
	public JSONArray listToTree(List<Folder> folders, String id, String pid, String child, String type) {
		JSONArray r = new JSONArray();
		JSONObject hash = new JSONObject();
		//将数组转为Object的形式，key为数组中的id
		JSONArray arr = new JSONArray();
		for (Folder folder : folders) {
			JSONObject json = new JSONObject();
			json.put(id, folder.getUuid());
			json.put(pid, folder.getParentId());
			json.put("label", folder.getDirName());
			json.put("type", type);
			json.put("level", folder.getDirId());
			hash.put(json.getString(id), json);
			arr.add(json);
		}
		//遍历结果集
		for (int j = 0; j < arr.size(); j++) {
			//单条记录
			JSONObject aVal = (JSONObject) arr.get(j);
			//在hash中取出key为单条记录中pid的值
			String pidStr = "";
			Object pidObj = aVal.get(pid);
			if (aVal.get(pid) != null) {
				pidStr = aVal.get(pid).toString();
			}
			JSONObject hashVP = (JSONObject) hash.get(pidStr);
			//如果记录的pid存在，则说明它有父节点，将她添加到孩子节点的集合中
			if (hashVP != null) {
				//检查是否有child属性
				if (hashVP.get(child) != null) {
					JSONArray ch = (JSONArray) hashVP.get(child);
					ch.add(aVal);
					hashVP.put(child, ch);
				} else {
					JSONArray ch = new JSONArray();
					ch.add(aVal);
					hashVP.put(child, ch);
				}
			} else {
				r.add(aVal);
			}
		}
		return r;
	}


	/**
	 * 描述: 关键词查询变量
	 * @param: [assets, keyword]
	 * @author: 周庚新
	 * @date: 2020/6/18
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.Assets>
	 */
	private Set<Assets> getVariablesAndRules(List<Assets> ruleAssets, List<Assets> variableAssets, String keyword) {
		Set<Assets> assetsList = new HashSet<>();
		List<String> fieldIds = new ArrayList<>();
		for (Assets asset : variableAssets) {
			String content = asset.getContent();
			if (StringUtils.isBlank(content)) {
				continue;
			}
			if (content.contains(keyword)) {
				assetsList.add(asset);
				JSONObject object = JSON.parseObject(content);
				JSONArray fieldArr = object.getJSONArray("fields");
				for (int i = 0; i < fieldArr.size(); i++) {
					JSONObject fieldObj = fieldArr.getJSONObject(i);
					String value = fieldObj.getString("name") + "," + fieldObj.getString("label");
					if (value.contains(keyword)) {
						assetsList.add(asset);
						String id = fieldObj.getString("id");
						fieldIds.add(id);
					}
				}
			}
		}
		if (CollectionUtils.isEmpty(fieldIds)) {
			return assetsList;
		}

		for (Assets asset : ruleAssets) {
			String content = asset.getContent();
			if (StringUtils.isBlank(content)) {
				continue;
			}
			for (String fieldId : fieldIds) {
				if (content.contains(fieldId)) {
					assetsList.add(asset);
					break;
				}
			}
		}
		return assetsList;
	}

	/**
	 * 描述: 关键词查询规则
	 * @param: [assets, keyword]
	 * @author: 周庚新
	 * @date: 2020/6/18
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.Assets>
	 */
	private List<Assets> getRules(List<Assets> assets, String keyword) {
		List<Assets> assetsList = new ArrayList<>();
		for (Assets asset : assets) {
			String name = asset.getName();
			String desc = asset.getDescription();
			if (StringUtils.isNotBlank(name) && name.contains(keyword)) {
				assetsList.add(asset);
				continue;
			}
			if (StringUtils.isNotBlank(desc) && desc.contains(keyword)) {
				assetsList.add(asset);
				continue;
			}

			if ("guidedrule".equals(asset.getType())) {
				String content = asset.getContent();
				if (StringUtils.isBlank(content)) {
					continue;
				}
				JSONObject object = JSON.parseObject(content);
				JSONArray ruleArray = object.getJSONArray("rules");
				for (int i = 0; i < ruleArray.size(); i++) {
					JSONObject rule = ruleArray.getJSONObject(i);
					String ruleName = rule.getString("name");
					if (StringUtils.isNotBlank(ruleName) && ruleName.contains(keyword)) {
						assetsList.add(asset);
						break;
					}
				}
			}
		}
		return assetsList;
	}

	private static String getFieldType(Field field) {
		if (Integer.class.equals(field.getType()) || "int".equals(field.getType().getName())) {
			return "Integer";
		} else if (String.class.equals(field.getType())) {
			return "String";
		} else if (Double.class.equals(field.getType()) || "double".equals(field.getType().getName())
				|| Float.class.equals(field.getType()) || "float".equals(field.getType().getName())) {
			return "Double";
		} else if (Long.class.equals(field.getType()) || "long".equals(field.getType().getName())) {
			return "Long";
		} else if (BigDecimal.class.equals(field.getType())) {
			return "BigDecimal";
		} else if (Boolean.class.equals(field.getType()) || "boolean".equals(field.getType().getName())) {
			return "Boolean";
		} else if (Date.class.equals(field.getType())) {
			return "Date";
		} else if (List.class.isAssignableFrom(field.getType())) {
			return "List";
		} else if (Set.class.isAssignableFrom(field.getType())) {
			return "Set";
		} else {
			return "Map";
		}
	}

	/**
	 * @auto: yinrj
	 * @description: .class模型文件解析成json
	 * @date: 2020/6/11 15:11
	 */
	private static String loadModelClassToJson(String tmpFilePath, String filename, Map<String, String> nameUuidMap) throws ClassNotFoundException {
		//类加载模型源文件
		if (!tmpFilePath.endsWith(File.separator)) {
			tmpFilePath += File.separator;
		}
		ClassLoader classLoader = new UploadFileClassLoader(tmpFilePath);
		String className = filename.substring(0, filename.length() - 6);
		Class clazz = classLoader.loadClass(className);
		Field[] fields = null;
		fields = clazz.getDeclaredFields();
		if (fields == null || fields.length == 0) {
			return "";
		}
		List<String> fieldStrs = new ArrayList<>();
		String parseResult = "{\"fields\":[";
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			int modifies = field.getModifiers();
			Class type = field.getType();
			if (Modifier.isStatic(modifies) || Modifier.isFinal(modifies) || "Log".equals(type.getSimpleName()) || "Logger".equals(type.getSimpleName())) {
				continue;
			}
			String fieldName = field.getName();
			//排除不支持的属性类型
			String fieldType = getFieldType(field);
			if (!StringUtils.isBlank(fieldType)) {
				//List类型的属性，其泛型必须为String、Integer、Double、Long、BigDecimal、Boolean、Date
				String subType = "";
				if ("List".equals(fieldType)) {
					subType = getListFieldSupportedGenericType(field);
					if (StringUtils.isNotBlank(subType) && subType.startsWith("Map@@")) {
						String name = subType.substring(5);
						if (name.equals(className)) {
							continue;
						}
						subType = nameUuidMap.get(name);
						if (StringUtils.isBlank(subType)) {
							subType = IdUtils.UUID();
							nameUuidMap.put(name, subType);
						}
					}
				}
				if ("Map".equals(fieldType)) {
					String clazzName = field.getType().getSimpleName();
					if (className.equals(className)) {
						continue;
					}

					subType = nameUuidMap.get(clazzName);
					if (StringUtils.isBlank(subType)) {
						subType = IdUtils.UUID();
						nameUuidMap.put(clazzName, subType);
					}

				}
				String fieldStr = "{\"id\":\"" + RandomCode.getSecurityCode(16, RandomCode.SecurityCodeLevel.Hard, true) + "\",\"name\":\"" + fieldName + "\",\"label\":\"" + fieldName + "\",\"type\":\"" + fieldType + "\",\"subType\":\"" + subType + "\",\"direction\":\"IN_OUT\"}";
				fieldStrs.add(fieldStr);
			}
		}
		if (CollectionUtils.isEmpty(fieldStrs)) {
			return "";
		}
		parseResult += StringUtils.join(fieldStrs, ",");
		parseResult += "]}";
		return parseResult;
	}

	/**
	 * @auto: yinrj
	 * @description: 属性为List的字段，校验泛型是否为：
	 * String、Integer、Double、Long、BigDecimal、Boolean、Date
	 * @date: 2020/6/11 15:11
	 */
	private static String getListFieldSupportedGenericType(Field field) {
		Type genericType = field.getGenericType();
		if (genericType != null) {
			// 如果是泛型参数的类型
			if (genericType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericType;
				//得到泛型里的class类型对象
				Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[0];
				if (String.class.equals(genericClazz)) {
					return "String";
				} else if (Integer.class.equals(genericClazz)) {
					return "Integer";
				} else if (Double.class.equals(genericClazz)) {
					return "Double";
				} else if (Long.class.equals(genericClazz)) {
					return "Long";
				} else if (BigDecimal.class.equals(genericClazz)) {
					return "BigDecimal";
				} else if (Boolean.class.equals(genericClazz)) {
					return "Boolean";
				} else if (Date.class.equals(genericClazz)) {
					return "Date";
				} else {
					return "Map@@" + genericClazz.getSimpleName();
				}
			}
		}
		return "";
	}

	/**
	 * 描述: 创建数据模型
	 * @param: [projectUuid, content, modelName, uuid]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: com.beagledata.gaea.workbench.entity.Assets
	 */
	private Assets createFact(String projectUuid, String content, String modelName, String uuid) {
		Assets assets = new Assets();
		assets.setUuid(uuid);
		assets.setProjectUuid(projectUuid);
		assets.setType("fact");
		assets.setDirParentId("0");
		assets.setCreator(UserHolder.currentUser());
		String name = copyName(new String(modelName), "fact", projectUuid);
		assets.setName(name);
		String enName = copyFactEnName(new String(modelName), projectUuid);
		assets.setEnName(enName);
		JSONObject object = JSON.parseObject(content);
		object.put("enName", enName);
		assets.setContent(object.toJSONString());
		return assets;
	}

	/**
	 * 描述: 替换数据模型的字段id
	 * @param: [content, fieldMap]
	 * @author: 周庚新
	 * @date: 2020/11/12
	 * @return: java.lang.String
	 */
	private String replaceFactId(String content, Map<String, String> fieldMap, Map<String, String> subTypeMappings) {
		if (StringUtils.isNotBlank(content)) {
			JSONObject contentObj = JSON.parseObject(content);
			JSONArray fileds = contentObj.getJSONArray("fields");
			for (int i = 0; i < fileds.size(); i++) {
				JSONObject field = fileds.getJSONObject(i);
				String oldFieldId = field.getString("id");
				String subType = field.getString("subType");
				if (StringUtils.isNotBlank(subType) && subType.length() == 32) {
					subTypeMappings.put(oldFieldId, subType);
				}
				String newFiledId = RandomCode.getSecurityCode(16, RandomCode.SecurityCodeLevel.Hard, true);
				while (fieldMap.containsValue(newFiledId)) {
					newFiledId = RandomCode.getSecurityCode(16, RandomCode.SecurityCodeLevel.Hard, true);
				}
				fieldMap.put(oldFieldId, newFiledId);
				field.put("id", newFiledId);
			}
			contentObj.put("fields", fileds);
			return contentObj.toJSONString();
		}
		return content;
	}
}
