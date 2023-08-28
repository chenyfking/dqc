package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.AssetsService;
import com.beagledata.gaea.workbench.service.ReferService;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.gaea.workbench.vo.VerifierVO;
import com.beagledata.util.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Auther: yinrj
 * @Date: 0018 2018/9/18 17:37
 * @Description:
 */
@RestController
@RequestMapping("assets")
public class AssetsController {
	@Autowired
	private AssetsService assetsService;
	@Autowired
	private ReferService referService;

	/**
	 * 条件查询资源文件列表
	 * @param page 当前页码
	 * @param pageNum 每页函数
	 * @param projectUuid 所属项目uuid
	 * @param name 文件名称
	 * @param type 文件类型
	 * @return
	 */
	@GetMapping("")
	public Result listOfParams(@RequestParam(required = false, defaultValue = "1") Integer page,
							   @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
							   String projectUuid, String name, String type) {
		if (StringUtils.isEmpty(projectUuid)) {
			throw new IllegalArgumentException("请求参数缺失！");
		}
		Map params = new HashMap();
		params.put("projectUuid", projectUuid);
		params.put("name", name);
		params.put("type", type);
		Map resultMap = new HashMap();
		resultMap.put("list", assetsService.listOfParams(page, pageNum, params));
		resultMap.put("total", assetsService.getTotal(params));
		return Result.newSuccess().withData(resultMap);
	}

	@PostMapping("add")
	@RequiresPermissions(PermissionSet.Project.CODE_ASSET_ADD)
	@RestLogAnnotation(describe = "新增资源文件")
	public Result addAssets(Assets assets) {
		assetsService.addAssets(assets);
		return Result.newSuccess().withData(assets.getUuid());
	}

	@PostMapping("edit")
	@RequiresPermissions(PermissionSet.Project.CODE_ASSET_EDIT)
	@RestLogAnnotation(describe = "编辑资源文件")
	public Result editOrAddAssets(Assets assets) {
		assetsService.editAssets(assets);
		return Result.newSuccess();
	}

	/**
	 * 资源文件编辑锁定或解锁
	 * @param uuids 资源uuid,多个用“,”分隔
	 * @param lock 是否锁定 1 锁定 0 解锁
	 */
	@PostMapping("lockedit")
	@RequiresPermissions(PermissionSet.Project.CODE_ASSET_EDIT)
	public Result updateEditor(String uuids, boolean lock) {
		return assetsService.updateEditor(uuids, lock, UserHolder.currentUserUuid());
	}

	/**
	 * @auto: yangyongqiang
	 * @Description:保存文件
	 * @Date: 2018-09-18 17:24
	 **/
	@PostMapping("savecontent")
	@RequiresPermissions(value = {PermissionSet.Project.CODE_ASSET_ADD, PermissionSet.Project.CODE_ASSET_EDIT}, logical = Logical.OR)
	@RestLogAnnotation(describe = "保存资源文件")
	public Result savecontent(Assets assets) {
		return assetsService.saveContent(assets);
	}

	/**
	 * @auto: yangyongqiang
	 * @Description:删除文件
	 * @Date: 2018-09-18 17:24
	 **/
	@PostMapping("delete")
	@RequiresPermissions(value = PermissionSet.Project.CODE_ASSET_DEL)
	@RestLogAnnotation(describe = "删除资源文件")
	public Result delete(String uuid) {
		return assetsService.delete(uuid);
	}

	/**
	 * @auto: yangyongqiang
	 * @Description:资源文件详情
	 * @Date: 2018-09-18 17:24
	 **/
	@GetMapping("{uuid}")
	public Result assets(@PathVariable String uuid,
						 @RequestParam(required = false, defaultValue = "0") int version) {
		return assetsService.selectAssets(uuid, version);
	}

	/**
	 * 根据文件id获得文件内容
	 * @param id
	 * @return
	 */
	@GetMapping("content/{id}")
	public Result byId(@PathVariable int id,
					   @RequestParam(required = false, defaultValue = "0") Integer versionNo) {
		return assetsService.selectContentById(id, versionNo);
	}

	/**
	 * @author yinrj
	 * @title listByProjectUuidAndTypeGroup
	 * @params [projetctUuid]
	 * @Description 根据
	 * @date 0021 2018/9/21 17:59
	 */
	@GetMapping("/group")
	public Result listOfParamsAndTypeGroup(String projectUuid) {
		if (StringUtils.isEmpty(projectUuid)) {
			throw new IllegalArgumentException("请求参数缺失！");
		}
		Set<String> typeList = assetsService.listTypeGroupByProjectUuid(projectUuid);
		List<String> types = new ArrayList<>();
		types.add(AssetsType.FACT);
		types.add(AssetsType.CONSTANT);
		types.add(AssetsType.GUIDED_RULE);
		types.add(AssetsType.RULE_TABLE);
		types.add(AssetsType.RULE_TREE);
		types.add(AssetsType.SCORECARD);
		types.add(AssetsType.RULE_FLOW);
		for (String type : types) {
			typeList.remove(type);
		}
		for (String type : typeList) {
			types.add(type);
		}
		List<Assets> assetsList = assetsService.listByProjectUuid(projectUuid);
		List dataList = new ArrayList();
		for (String type : types) {
			Map map = new HashMap();
			List<Assets> list = new ArrayList<>();
			map.put("type", type);
			for (Assets assets : assetsList) {
				if (type.equals(assets.getType())) {
					list.add(assets);
				}
			}
			map.put("list", list);
			dataList.add(map);
		}
		return Result.newSuccess().withData(dataList);
	}

	/**
	 * 保存新版本
	 * @author chenyafeng
	 * @date 2018/11/2
	 */
	@PostMapping("version/new")
	@RequiresPermissions(value = PermissionSet.Project.CODE_ASSET_EDIT)
	@RestLogAnnotation(describe = "保存新版本资源文件")
	public Result newVersion(Assets assets) {
		assetsService.addNewVersion(assets);
		return Result.newSuccess();
	}

	/**
	 * 版本切换
	 * @param assets
	 * @return
	 * @author chenyafeng
	 */
	@PostMapping("/version/change")
	@RestLogAnnotation(describe = "资源文件版本切换")
	public Result changeVersion(Assets assets) {
		return assetsService.changeVersion(assets);
	}

	/**
	 * 版本列表
	 * @author chenyafeng
	 * @date 2018/11/2
	 * /version/list
	 */
	@GetMapping("/version/list")
	public Result versionList(String uuid,
							  @RequestParam(required = false, defaultValue = "1") int page,
							  @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum) {
		return assetsService.listVersionsByUuid(uuid, page, pageNum);
	}

	/**
	 * 删除版本
	 * @param assets
	 * @return
	 * @author chenyafeng
	 */
	@PostMapping("/version/delete")
	@RestLogAnnotation(describe = "删除资源文件版本")
	@RequiresPermissions(value = PermissionSet.Project.CODE_ASSET_DEL)
	public Result versionDelete(Assets assets) {
		return assetsService.deleteVersion(assets);
	}

	@PostMapping("lock")
	@RequiresPermissions(value = PermissionSet.Project.CODE_ASSET_LOCK)
	@RestLogAnnotation(describe = "锁定资源文件")
	public Result lock(String uuid) {
		return assetsService.lockAssets(uuid, true);
	}

	@PostMapping("unlock")
	@RequiresPermissions(value = PermissionSet.Project.CODE_ASSET_LOCK)
	@RestLogAnnotation(describe = "解锁资源文件")
	public Result unlock(String uuid) {
		return assetsService.lockAssets(uuid, false);
	}

	/**
	 * @Author: mahongfei
	 * @description: 根据文件名称和文件夹名称模糊查询文件
	 */
	@GetMapping("/query")
	public Result listOfAssetNameAndFolderName(Assets assets) {
		return assetsService.listOfAssetNameAndFolderName(assets);
	}

	/**
	 * 描述: 规则文件比较
	 * @param: [assetUuid, assetType, baseVersion, compareVersion] 文件uuid 文件类型 基础版本 ，比较版本
	 * @author: 周庚新
	 * @date: 2020/7/14
	 * @return: com.beagledata.common.Result
	 */
	@GetMapping("compare")
	public Result getCompareContent(String assetUuid, String assetType, Integer baseVersion, Integer compareVersion) {
		return assetsService.getCompareContent(assetUuid, assetType, baseVersion, compareVersion);
	}

	/**
	 * 规则复制
	 */
	@PostMapping("copy")
	public Result rulesCopy(Assets assets) {
		Assets newAssets = assetsService.rulesCopy(assets);
		return Result.newSuccess().withData(newAssets);
	}

	/**
	 * @author yinrj
	 * @title importJavaFile
	 * @params [projectUuid, file]
	 * @description 导入数据模型jar或class或java文件
	 * @date 0008 2020/6/8 14:30
	 */
	@PostMapping("importJavaFile")
	public Result importJavaFile(String projectUuid, MultipartFile file) {
		return assetsService.importJavaFile(projectUuid, file);
	}


	/**
	 * 描述: 根据关键词搜索变量或者规则
	 * @param: [projectUuid, keyword, type] 项目uuid  查询关键词 查询类型 （rule 查询规则，variable 查询变量）
	 * @author: 周庚新
	 * @date: 2020/6/18
	 * @return: com.beagledata.common.Result
	 */
	@GetMapping("search")
	public Result search(String projectUuid, String keyword, String type) {
		return assetsService.searchVariablesOrRules(projectUuid, keyword, type);
	}

	/**
	 * 描述: 决策建模（文件资源）左侧树
	 * @param: [parentUuid, type]
	 * @author: 周庚新
	 * @date: 2020/6/22
	 * @return: com.beagledata.common.Result
	 */
	@GetMapping("tree")
	public Result tree(String projectUuid, String parentUuid, String type) {
		return assetsService.getAssetsTreeNode(projectUuid, parentUuid, type);
	}

	/**
	 * 描述:获取某一个分类下边的 文件夹树 （只有文件夹）
	 * @param: [projectUuid, parentUuid, type]
	 * @author: 周庚新
	 * @date: 2020/6/23
	 * @return: com.beagledata.common.Result
	 */
	@GetMapping("folderTree")
	public Result folderTree(String projectUuid, String type) {
		return assetsService.getFolderTree(projectUuid, type);
	}

	/**
	 * 资源被引用列表查询
	 * @author yinrj
	 * @date 2020/7/6
	 */
	@GetMapping("reference")
	public Result reference(String assetsUuid, Integer versionNo) {
		return referService.selectReferBySubjectUuidAndVersion(assetsUuid, versionNo);
	}

	@PostMapping("verifier")
	public Result verifer(VerifierVO verifierVO) {
		return assetsService.verifier(verifierVO);
	}

	@GetMapping("export")
	@RestLogAnnotation(describe = "导出规则")
	public void export(String uuid, HttpServletResponse response) {
		assetsService.export(uuid, response);
	}

	@PostMapping("import")
	@RestLogAnnotation(describe = "导入规则")
	public Result importRule(String projectUuid, MultipartFile file) {
		return assetsService.importRule(projectUuid, file);
	}
}
