package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.service.LogsService;
import com.beagledata.gaea.workbench.service.ProjectService;
import com.beagledata.gaea.workbench.service.UserService;
import com.beagledata.gaea.workbench.util.UserHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yangyongqiang 2019/09/12
 */
@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
	private LogsService logsService;
    @Autowired
    private ProjectService projectService;
    private static String pattern = "[0-9]{4}-[0-9]{2}-[0-9]{2}";

    @GetMapping("profile")
    public Result getProfile() {
        return Result.newSuccess().withData(UserHolder.currentUser());
    }

    @GetMapping("search")
    @RequiresPermissions(value = PermissionSet.User.CODE_VIEW)
    public Result<Map<String, Object>> search(@RequestParam(required = false, defaultValue = "1") int page,
                                              @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum,
                                              User user) {
        return userService.search(page, pageNum, user);
    }

    @GetMapping("searchnotprojectmember")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PROJECT_VIEW)
    public Result searchNotProjectMember(@RequestParam(required = false, defaultValue = "1") int page,
                                         @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum,
                                         User user, String projectUuid) {
        return userService.searchNotProjectMember(page, pageNum, user, projectUuid);
    }

    @PostMapping("add")
    @RequiresPermissions(value = PermissionSet.User.CODE_ADD)
    @RestLogAnnotation(describe = "添加用户")
    public Result add(User user, String roleIds) {
        return Result.newSuccess().withData(userService.add(user, roleIds));
    }

    @PostMapping("delete")
    @RestLogAnnotation(describe = "删除用户")
    @RequiresPermissions(value = PermissionSet.User.CODE_DELETE)
    public Result delete(String uuid) {
        userService.delete(uuid);
        return Result.newSuccess();
    }

    @PostMapping("enable")
    @RestLogAnnotation(describe = "启用用户")
    @RequiresPermissions(value = PermissionSet.User.CODE_EDIT_VALIDATION)
    public Result enable(String uuid) {
        userService.enable(uuid);
        return Result.newSuccess();
    }

    @PostMapping("disable")
    @RequiresPermissions(value = PermissionSet.User.CODE_EDIT_VALIDATION)
    @RestLogAnnotation(describe = "禁用用户")
    public Result disable(String uuid) {
        userService.disable(uuid);
        return Result.newSuccess();
    }

    @PostMapping("editmypwd")
    @RestLogAnnotation(describe = "修改密码")
    public Result editMyPwd(String oldPassword, String newPassword) {
        userService.editMyPwd(oldPassword, newPassword);
        return Result.newSuccess();
    }

    @GetMapping("onlines")
    @RequiresPermissions(value = PermissionSet.Login.CODE_VIEW)
    public Result<Map<String, Object>> search(@RequestParam(required = false, defaultValue = "1") int page,
                                              @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum,
                                              @RequestParam(required = false) String sortField,
                                              @RequestParam(required = false) String sortDirection) {
        return userService.listAllOnline(page, pageNum, sortField, sortDirection);
    }

    @PostMapping("forcelogout")
    @RestLogAnnotation(describe = "强制下线用户")
    @RequiresPermissions(PermissionSet.Login.CODE_OFFLINE)
    public Result forceLogOut(String uuid,String loginIp) {
        userService.forceLogOut(uuid,loginIp);
        return Result.newSuccess();
    }

    @PostMapping("edit")
    @RestLogAnnotation(describe = "编辑用户")
    @RequiresPermissions(value = PermissionSet.User.CODE_EDIT)
    public Result edit(User user, String roleIds) {
        userService.edit(user, roleIds);
        return Result.newSuccess();
    }

    /**
	 * 查询日志列表(没有数据隔离)
	 * @auth yinrj
	 * @date 2021/1/22
	 * @return
	 */
    @GetMapping("logs")
    public Result queryLogs(@RequestParam(required = false, defaultValue = "1") Integer page,
                            @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
                            String user,
                            String success,
                            String clientIp,
                            String optName,
                            String beginTime,
                            String endTime,
                            String userUuid) {
        Map<String, Object> params = new HashMap<>();
		params.put("start", pageNum * (page - 1));
		params.put("limit", pageNum);
		if (StringUtils.isNotBlank(user)) {
			params.put("user", user);
		}
		if (StringUtils.isNotBlank(success)) {
			params.put("success", success);
		}
		if (StringUtils.isNotBlank(userUuid)) {
			params.put("userUuid", userUuid);
		}
		if (StringUtils.isNotBlank(clientIp)) {
			params.put("clientIp", clientIp);
		}
		if (StringUtils.isNotBlank(optName)) {
			params.put("optName", optName);
		}
		if (StringUtils.isNotBlank(beginTime)) {
			if (!beginTime.matches(pattern)) {
				return Result.newError().withMsg("开始时间格式出错");
			}
			params.put("beginTime", beginTime + " 00:00:00");
		}
		if (StringUtils.isNotBlank(endTime)) {
			if (!endTime.matches(pattern)) {
				return Result.newError().withMsg("结束时间格式出错");
			}
			params.put("endTime", endTime + " 23:59:59");
		}

		int total = logsService.countTotalWithoutDataIsolate(params);
		if (total > 0) {
			return Result.newSuccess().withData(logsService.listPageWithoutDataIsolate(params)).withTotal(total);
		}
		return Result.emptyList();
    }

    /**
	 * 查询登录日志列表
	 * @auth yinrj
	 * @date 2021/1/25
	 * @return
	 */
    @GetMapping("login/logs")
    public Result queryLoginLogs(@RequestParam(required = false, defaultValue = "1") Integer page,
                            @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
                            String success,
                            String clientIp,
                            String beginTime,
                            String endTime,
                            String userUuid) {
        Map<String, Object> params = new HashMap<>();
		params.put("start", pageNum * (page - 1));
		params.put("limit", pageNum);
		if (StringUtils.isNotBlank(success)) {
			params.put("success", success);
		}
		if (StringUtils.isNotBlank(userUuid)) {
			params.put("userUuid", userUuid);
		}
		if (StringUtils.isNotBlank(clientIp)) {
			params.put("clientIp", clientIp);
		}
        params.put("optName", "用户登录");
		if (StringUtils.isNotBlank(beginTime)) {
			if (!beginTime.matches(pattern)) {
				return Result.newError().withMsg("开始时间格式出错");
			}
			params.put("beginTime", beginTime + " 00:00:00");
		}
		if (StringUtils.isNotBlank(endTime)) {
			if (!endTime.matches(pattern)) {
				return Result.newError().withMsg("结束时间格式出错");
			}
			params.put("endTime", endTime + " 23:59:59");
		}

		int total = logsService.countTotalOfLoginLogs(params);
		if (total > 0) {
			return Result.newSuccess().withData(logsService.listPageOfLoginLogs(params)).withTotal(total);
		}
		return Result.emptyList();
    }

    /**
     * @Author yangyongqiang
     * @Description 获取用户详情
     * @Date 上午10:22 2021/1/25
     **/
    @GetMapping("{uuid}")
    //@RequiresPermissions(value = PermissionSet.User.CODE_VIEW)
    public Result getUser(@PathVariable("uuid") String uuid) {
        return userService.getUserDetails(uuid);
    }


    /**
	 * 查询项目列表
	 * @auth yinrj
	 * @date 2021/1/22
	 * @return
	 */
    @GetMapping("project")
    public Result queryLoginLogs(@RequestParam(required = false, defaultValue = "1") Integer page,
                            @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
                            @RequestParam(required = false) String sortField,
							@RequestParam(required = false) String sortDirection,
                            String userUuid, String projectName) {
        return projectService.listProjectsByUser(page, pageNum, userUuid, projectName, sortField, sortDirection);
    }
}
