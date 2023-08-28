package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.service.ProjectUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Cyf on 2019/12/16
 **/
@RestController
@RequestMapping("pu")
public class ProjectUserController {
    @Autowired
    private ProjectUserService projectUserService;

    /**
     * 查看项目关联的用户
     * Created by Chenyafeng on 2019/12/16
     */
    @GetMapping("users")
    public Result users(String uuid) {
        return Result.newSuccess().withData(projectUserService.selectUserByProject(uuid));
    }

    /**
     * 项目批量关联用户
     * @param projectUuid
     * @param uuids
     * @return
     */
    @PostMapping("auth")
    @RestLogAnnotation(describe = "批量添加项目成员")
    public Result auth(String projectUuid, String uuids) {
        projectUserService.batchRelation(projectUuid, uuids);
        return Result.newSuccess();
    }
}