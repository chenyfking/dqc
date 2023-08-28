package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.FunctionDefinition;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.FunctionDefinitionService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 方法，函数
 * Created by Cyf on 2019/8/26
 **/
@RestController
@RequestMapping("function")
public class FunctionDefinitionController {
    @Autowired
    private FunctionDefinitionService functionDefinitionService;

    /**
     * 获取所有函数集，规则建模调用
     *
     * @return
     */
    @GetMapping("")
    public Result list() {
        return Result.newSuccess().withData(functionDefinitionService.list());
    }

    /**
     * 获取所有函数集
     *
     * @return
     */
    @GetMapping("all")
    @RequiresPermissions(value = PermissionSet.Project.CODE_FUNCTION_VIEW)
    public Result all() {
        return Result.newSuccess().withData(functionDefinitionService.list());
    }

    /**
     * 添加函数集
     *
     * @param src
     * @return
     */
    @PostMapping("add")
    @RequiresPermissions(value = PermissionSet.Project.CODE_FUNCTION_ADD)
    public Result add(String src) {
        String uuid = functionDefinitionService.add(src);
        return Result.newSuccess().withData(uuid);
    }

    @PostMapping("edit")
    @RequiresPermissions(value = PermissionSet.Project.CODE_FUNCTION_EDIT)
    public Result add(String uuid, String src) {
        return functionDefinitionService.edit(uuid, src);
    }

    /**
     * 上传函数包
     *
     * @param file
     * @return
     */
    @PostMapping("upload")
    @RequiresPermissions(value = PermissionSet.Project.CODE_FUNCTION_ADD)
    public Result upload(MultipartFile file) {
        functionDefinitionService.upload(file);
        return Result.newSuccess();
    }

    /**
     * 删除函数集
     *
     * @param uuid
     * @return
     */
    @PostMapping("delete")
    @RequiresPermissions(value = PermissionSet.Project.CODE_FUNCTION_DELETE)
    public Result delete(String uuid) {
        return functionDefinitionService.delete(uuid);
    }

    /**
     * 查看函数集
     *
     * @param uuid
     * @return
     */
    @GetMapping("view")
    public Result view(String uuid) {
        FunctionDefinition definition = functionDefinitionService.getByUuid(uuid);
        return Result.newSuccess().withData(definition.getContent());
    }

    /**
     * 下载函数jar包
     *
     * @param uuid
     * @param response
     */
    @GetMapping("download")
    public void download(String uuid, HttpServletResponse response) {
        functionDefinitionService.downloadByUuid(uuid, response);
    }
}