package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.entity.Project;
import com.beagledata.gaea.workbench.service.ProjectService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by mahongfei on 2018/9/18.
 */
@RestController
@RequestMapping("project")
public class ProjectController {
	@Autowired
	private ProjectService projectService;

	@PostMapping("add")
	@RequiresPermissions(value = PermissionSet.Project.CODE_PROJECT_ADD)
	@RestLogAnnotation(describe = "添加项目")
	public Result addOrEditProject(Project project) {
		return Result.newSuccess().withData(projectService.addProject(project));
	}

	@PostMapping("edit")
	@RestLogAnnotation(describe = "编辑项目")
	public Result editProject(Project project) {
		projectService.editProject(project);
		return Result.newSuccess();
	}

	/**
	 * @Author: mahongfei
	 * @description: 删除项目
	 */
	@PostMapping("delete")
	@RestLogAnnotation(describe = "删除项目")
	public Result deleteProject(String uuid) {
		return projectService.deleteProject(uuid);
	}

	/**
	 * @Author: mahongfei
	 * @description: 项目列表
	 */
	@GetMapping("")
	public Result listAll(@RequestParam(required = false, defaultValue = "1") int page,
											   @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum,
											   @RequestParam(required = false) String sortField,
											   @RequestParam(required = false) String sortDirection,
											   String name) {
		return projectService.listPage(page, pageNum, name, sortField, sortDirection);
	}

	/**
	 * @Author: mahongfei
	 * @description: 项目详情
	 */
	@GetMapping("{uuid}")
	public Result<Project> projectDetails(@PathVariable String uuid) {
		return Result.newSuccess().withData(projectService.projectDetails(uuid));
	}

	/**
	 * @Author: mahongfei
	 * @description: 项目导出
	 */
	@GetMapping("export")
    @RestLogAnnotation(describe = "导出项目")
	@RequiresPermissions(value = PermissionSet.Project.CODE_PROJECT_EXPORT)
	public Result exportProject(String uuid, HttpServletResponse response) {
		projectService.export(uuid, response);
		return Result.newSuccess();
	}

	/**
	 * @Author: mahongfei
	 * @description: 项目导入
	 */
	@PostMapping("import")
    @RestLogAnnotation(describe = "导入项目")
	@RequiresPermissions(value = PermissionSet.Project.CODE_PROJECT_IMPORT)
	public Result importProject(MultipartFile file) throws IOException {
		return projectService.importProject(file.getBytes());
	}
}
