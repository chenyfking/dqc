package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.LogsService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: LogsController
 * @Description: 日志功能
 * @author: yinrj
 * @date 2018年8月16日 下午4:37:44
 */
@RestController
@RequestMapping("logs")
public class LogsController {
	@Autowired
	private LogsService logsService;
	private static String pattern = "[0-9]{4}-[0-9]{2}-[0-9]{2}";

	/**
	 * @throws
	 * @title: queryByParamsAndPage
	 * @param: page 当前页码
	 * @param: rows 每页行数
	 * @param: paramsMap 查询条件
	 * @description: 动态参数分页查询日志
	 * @date:2018年8月16日下午3:55:33
	 * @author: yinrj
	 */
	@GetMapping("")
	@RequiresPermissions(value = PermissionSet.Log.CODE_VIEW)
	public Result queryByParamsAndPage(@RequestParam(required = false, defaultValue = "1") Integer page,
									   @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
									   String user,
									   String success,
									   String clientIp,
									   String optName,
									   String beginTime,
									   String endTime) {
		Map<String, Object> params = new HashMap<>();
		params.put("start", pageNum * (page - 1));
		params.put("limit", pageNum);
		if (StringUtils.isNotBlank(user)) {
			params.put("user", user);
		}
		if (StringUtils.isNotBlank(success)) {
			params.put("success", success);
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

		int total = logsService.countTotal(params);
		if (total > 0) {
			return Result.newSuccess().withData(logsService.listPage(params)).withTotal(total);
		}
		return Result.emptyList();
	}
}
