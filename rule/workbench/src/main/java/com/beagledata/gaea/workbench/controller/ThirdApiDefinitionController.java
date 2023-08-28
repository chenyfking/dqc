package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.entity.ThirdApiDefinition;
import com.beagledata.gaea.workbench.service.ThirdApiDefinitionService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by liulu on 2020/6/14.
 */
@RestController
@RequestMapping("thirdapi")
public class ThirdApiDefinitionController {
    @Autowired
    private ThirdApiDefinitionService thirdApiDefinitionService;

    /**
     * 建模用到的所有外部接口
     *
     * @return
     */
    @GetMapping("")
    public Result listForModeling() {
        return Result.newSuccess().withData(thirdApiDefinitionService.listForModeling());
    }

    /**
     * 添加自定义外部接口
     *
     * @param definition
     * @return
     */
    @PostMapping("add")
    @RequiresPermissions(value = PermissionSet.Project.CODE_THIRDAPI_ADD)
    public Result add(ThirdApiDefinition definition) {
        thirdApiDefinitionService.add(definition);
        return Result.newSuccess();
    }

    /**
     * 获取所有外部接口
     *
     * @return
     */
    @GetMapping("all")
    @RequiresPermissions(value = PermissionSet.Project.CODE_THIRDAPI_VIEW)
    public Result all() {
        return Result.newSuccess().withData(thirdApiDefinitionService.list());
    }

    /**
     * 删除外部接口
     *
     * @param uuid
     * @return
     */
    @PostMapping("delete")
    @RequiresPermissions(value = PermissionSet.Project.CODE_THIRDAPI_DELETE)
    public Result delete(String uuid) {
        return thirdApiDefinitionService.delete(uuid);
    }

    /**
     * 编辑外部接口
     *
     * @param definition
     * @return
     */
    @PostMapping("edit")
    @RequiresPermissions(value = PermissionSet.Project.CODE_THIRDAPI_EDIT)
    public Result edit(ThirdApiDefinition definition) {
        thirdApiDefinitionService.edit(definition);
        return Result.newSuccess();
    }
}
