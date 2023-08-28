package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.MicroDeployment;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.MicroDeploymentService;
import com.beagledata.gaea.workbench.service.MicroRouteService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author yangyongqiang
 **/
@RestController
@RequestMapping("microdeployment")
public class MicroDeploymentController {
	@Autowired
	private MicroDeploymentService microDeploymentService;
	@Autowired
	private MicroRouteService microRouteService;

	@GetMapping("")
	public Result listPage(@RequestParam(required = false, defaultValue = "1") Integer page,
						  @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
						  String microUuid) {
		return microDeploymentService.listPage(page, pageNum, microUuid);
	}

	/**
     * 服务生效(单模型上线或A/B测试及冠军挑战者上线)
     * @author yinrj
     * @date 2020/7/14
     */
    @PostMapping("deploy")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_MICRO_ONLINE)
    @RestLogAnnotation(describe = "服务生效上线")
    public Result deploy(MicroDeployment microDeployment) {
        microDeploymentService.deploy(microDeployment);
        microRouteService.refreshMicroRouteToRedis();
        return Result.newSuccess();
    }

    @PostMapping("offline")
	@RequiresPermissions(value = PermissionSet.Micro.CODE_MICRO_SHUTDOWN)
    @RestLogAnnotation(describe = "停用服务")
	public Result offline(String microUuid, String packageUuid) {
    	microDeploymentService.offline(microUuid, packageUuid);
        microRouteService.refreshMicroRouteToRedis();
        return Result.newSuccess();
	}
}
