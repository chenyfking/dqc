package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: yinrj
 * @Date: 0022 2021/1/22 09:41
 * @Description:
 */
@RestController
@RequestMapping("role")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping("users")
    @RestLogAnnotation(describe = "查看用户")
//    @RequiresPermissions(value = PermissionSet.Role.CODE_VIEWUSER)
    public Result selectUsers(@RequestParam(required = false, defaultValue = "1") Integer page,
                              @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
                              Integer roleId, String keywords) {
        return roleService.selectUsers(page, pageNum, roleId, keywords);
    }
}
