package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.Org;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.OrgService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
* 描述: 机构管理
* @author: 周庚新
* @date: 2020/6/1
*/
@RestController
@RequestMapping("org")
public class OrgController {

    @Autowired
    private OrgService orgService;

    /**
    * 描述: 查询机构列表
    * @param: [page, pageNum, org]
    * @author: 周庚新
    * @date: 2020/6/1
    * @return: com.beagledata.common.Result<java.util.Map<java.lang.String,java.lang.Object>>
    */
    @GetMapping("")
	@RequiresPermissions(value = PermissionSet.Org.CODE_VIEW)
    public Result<Map<String, Object>> search(@RequestParam(required = false, defaultValue = "1") Integer page,
                                              @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
                                              Org org) {
        return orgService.search(page, pageNum, org);
    }

    /**
    * 描述:添加机构
    * @param: [org]
    * @author: yyq
    * @date: 2020/6/1
    * @return: com.beagledata.common.Result
    */
    @PostMapping("add")
    @RequiresPermissions(value = PermissionSet.Org.CODE_ADD)
    @RestLogAnnotation(describe = "添加机构")
    public Result add(Org org) {
        return Result.newSuccess().withData(orgService.add(org));
    }

    /**
    * 描述: 删除机构
    * @param: [uuid]
    * @author: 周庚新
    * @date: 2020/6/1
    * @return: com.beagledata.common.Result
    */
    @PostMapping("delete")
    @RestLogAnnotation(describe = "删除机构")
    @RequiresPermissions(value = PermissionSet.Org.CODE_DELETE)
    public Result delete(String uuid) {
		orgService.delete(uuid);
        return Result.newSuccess();
    }

    /**
    * 描述: 编辑机构
    * @param: [org]
    * @author: yyq
    * @date: 2020/6/1
    * @return: com.beagledata.common.Result
    */
    @PostMapping("edit")
    @RestLogAnnotation(describe = "编辑机构")
    @RequiresPermissions(value = PermissionSet.Org.CODE_EDIT)
    public Result edit(Org org) {
		orgService.edit(org);
        return Result.newSuccess();
    }

    /**
     * 描述:查询所有机构
     * @param: [org]
     * @author: yyq
     * @date: 2020/6/2
     * @return: com.beagledata.common.Result
     */
    @GetMapping("all")
    public Result selectAll() {
        return orgService.selectAll();
    }

    @GetMapping("top50")
    @RequiresPermissions(value = {
            PermissionSet.Org.CODE_VIEW, PermissionSet.User.CODE_EDIT, PermissionSet.User.CODE_ADD
    }, logical = Logical.OR)
    public Result search(String name) {
        return orgService.listTop50ByName(name);
    }

    /**
     *根据所属机构查询用户
     * @param page
     * @param pageNum
     * @param orgUuid
     * @author: yinrj
     * @date: 2021/1/21
     * @return
     */
    @GetMapping("users")
    @RestLogAnnotation(describe = "查看用户")
//    @RequiresPermissions(value = PermissionSet.Org.CODE_VIEWUSER)
    public Result selectUsers(@RequestParam(required = false, defaultValue = "1") Integer page,
                              @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
                              String orgUuid, String keywords) {
        return orgService.selectUsers(page, pageNum, orgUuid, keywords);
    }
}
