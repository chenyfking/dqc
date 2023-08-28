package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.AiModel;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.AiModelService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by liulu on 2018/1/17.
 */
@RestController
@RequestMapping("aimodel")
public class AiModelController {
	@Autowired
	private AiModelService aiModelService;

	/**
	 * @return 编辑规则的时候获取所有模型
	 */
	@GetMapping("")
	public Result listAllForAssets() {
		AiModel aiModel = new AiModel();
		aiModel.setEnable(true);
		return aiModelService.listAll(1, 10, aiModel, true, null, null);
	}

	@GetMapping("all")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_AIMODEL_VIEW)
	public Result listAll(@RequestParam(required = false, defaultValue = "1") int page,
						  @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum,
						  @RequestParam(required = false, defaultValue = "false") boolean isAll,
						  @RequestParam(required = false) String sortField,
						  @RequestParam(required = false) String sortDirection,
						  AiModel aiModel) {
		return aiModelService.listAll(page, pageNum, aiModel, isAll, sortField, sortDirection);
	}

	@PostMapping("add")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_AIMODEL_ADD)
	@RestLogAnnotation(describe = "添加AI模型")
	public Result add(AiModel model) {
		aiModelService.add(model);
		return Result.newSuccess().withData(model.getUuid());
	}

	@PostMapping("edit")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_AIMODEL_EDIT)
	@RestLogAnnotation(describe = "编辑AI模型")
	public Result edit(AiModel model) {
		aiModelService.edit(model);
		return Result.newSuccess();
	}

	@PostMapping("delete")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_AIMODEL_DEL)
	@RestLogAnnotation(describe = "删除AI模型")
	public Result delete(String uuid) {
		return aiModelService.delete(uuid);
	}

	@PostMapping("enable")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_AIMODEL_EDIT)
	@RestLogAnnotation(describe = "启用AI模型")
	public Result enable(String uuid) {
		aiModelService.enable(uuid);
		return Result.newSuccess();
	}

	@PostMapping("disable")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_AIMODEL_EDIT)
	@RestLogAnnotation(describe = "禁用AI模型")
	public Result disable(String uuid) {
		aiModelService.disable(uuid);
		return Result.newSuccess();
	}

	/**
	 * 下载模型
	 *
	 * @author chenyafeng
	 * @date 2018/6/25
	 */
	@GetMapping("down")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_AIMODEL_DOWNLOAD)
	@RestLogAnnotation(describe = "下载AI模型")
	public Result down(String uuid, HttpServletResponse response) {
		return aiModelService.downModel(uuid, response);
	}

	/**
	 * 查询ai模型关联的服务
	 *
	 * @author chenyafeng
	 * @date 2018/12/18
	 */
	@GetMapping("micros")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_AIMODEL_VIEW)
	public Result micros(String uuid) {
		return aiModelService.microForModel(uuid);
	}

	/**
	 * 下载模型参数
	 */
	@GetMapping("downmodelparams")
	@RestLogAnnotation(describe = "下载ai模型参数")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_AIMODEL_DOWNLOAD)
	public Result downModelParams(String uuid, HttpServletResponse response) {
		return aiModelService.downModelParams(uuid, response);
	}
}
