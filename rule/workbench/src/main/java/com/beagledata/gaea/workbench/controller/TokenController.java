package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.entity.Token;
import com.beagledata.gaea.workbench.service.MicroService;
import com.beagledata.gaea.workbench.service.TokenService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 描述: 令牌管理
 * @author: 周庚新
 * @date: 2020/6/11
 */
@RestController
@RequestMapping("token")
public class TokenController {
	@Autowired
	private TokenService tokenService;
	@Autowired
    private MicroService microService;

    @GetMapping("")
    @RequiresPermissions(PermissionSet.Micro.CODE_TOKEN_VIEW)
    public Result<Map<String, Object>> list(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum,
            Token token) {
        return tokenService.listPage(page, pageNum, token);
    }

    @PostMapping("add")
    @RequiresPermissions(PermissionSet.Micro.CODE_TOKEN_ADD)
    @RestLogAnnotation(describe = "添加令牌")
    public Result add(String name, String microUuids, boolean all) {
        tokenService.add(name, microUuids, all);
        tokenService.refreshToken();
        return Result.SUCCESS;
    }

    @PostMapping("delete")
    @RequiresPermissions(PermissionSet.Micro.CODE_TOKEN_DEL)
    @RestLogAnnotation(describe = "删除令牌")
    public Result delete(String uuid) {
        tokenService.delete(uuid);
        tokenService.refreshToken();
        return Result.SUCCESS;
    }

    @PostMapping("edit")
    @RequiresPermissions(PermissionSet.Micro.CODE_TOKEN_EDIT)
    @RestLogAnnotation(describe = "编辑令牌")
    public Result edit(String uuid, String name, String microUuids, boolean all) {
        tokenService.edit(uuid, name, microUuids, all);
        tokenService.refreshToken();
        return Result.SUCCESS;
    }

    @GetMapping("micro")
    @RequiresPermissions(value = {PermissionSet.Micro.CODE_TOKEN_ADD, PermissionSet.Micro.CODE_TOKEN_EDIT}, logical = Logical.OR)
    public Result listMicro() {
        return Result.newSuccess().withData(microService.listAllForToken());
    }
}
