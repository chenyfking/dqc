package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.KnowledgePackageBaseline;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.KnowledgePackageBaselineService;
import com.beagledata.gaea.workbench.service.MicroRouteService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: yinrj
 * @Date: 0028 2020/6/28 16:58
 * @Description: 知识基线管理
 */
@RestController
@RequestMapping("baseline")
public class KnowledgePackageBaselineController {
    @Autowired
    private KnowledgePackageBaselineService baselineService;
    @Autowired
    private MicroRouteService microRouteService;

    /**
     * 基线列表查询
     * @auther yinrj
     * @date 2020/6/29
     * @param packageUuid
     */
    @GetMapping("")
    @RequiresPermissions(PermissionSet.Project.CODE_BASELINE_VIEW)
    public Result list(@RequestParam(required = false, defaultValue = "1") Integer page,
                       @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
                       String packageUuid) {
        return Result.newSuccess().withData(baselineService.listByPackageUuid(page, pageNum, packageUuid))
                .withTotal(baselineService.getCountByPackageUuid(packageUuid));
    }

    /**
     * @Author yangyongqiang
     * @Description 决策服务知识包基线查询
     * @Date 9:54 上午 2020/7/3
     **/
    @GetMapping("microbaseline")
    public Result listMicroBaseline(@RequestParam(required = false, defaultValue = "1") Integer page,
                       @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) Integer pageNum,
                       String packageUuid) {
        return Result.newSuccess().withData(baselineService.listMicroBaselineByPackageUuid(page, pageNum, packageUuid))
                .withTotal(baselineService.getCountMicroBaselineByPackageUuid(packageUuid));
    }

    /**
     * 保存基线
     * @auther yinrj
     * @date 2020/6/28
     * @param
     */
    @PostMapping("save")
	@RequiresPermissions(value = PermissionSet.Project.CODE_BASELINE_ADD)
    public Result add(String packageUuid, String assets) {
        return baselineService.add(packageUuid, assets);
    }

    /**
     * @Author yangyongqiang
     * @Description 删除知识包基线接口
     * @Date 9:31 上午 2020/7/1
     * @Param [uuid]
     * @return com.beagledata.common.Result
     **/
    @PostMapping("delete")
	@RequiresPermissions(value = PermissionSet.Project.CODE_BASELINE_DEL)
    public Result delete(String uuid) {
        baselineService.delete(uuid);
        return Result.newSuccess();
    }

    /**
     * 发布基线生成服务
     * @author yinrj
     * @date 2020/7/1
     */
    @PostMapping("publish")
	@RequiresPermissions(value = PermissionSet.Project.CODE_BASELINE_PUBLISH)
    @RestLogAnnotation(describe = "知识包基线发布")
    public Result publishMicro(String packageUuid, Integer baselineVersion) {
        baselineService.publish(packageUuid, baselineVersion);
        return Result.newSuccess();
    }

    /**
     * @Author yangyongqiang
     * @Description 知识包发布审核
     * @Date 10:16 上午 2020/7/6
     **/
    @PostMapping("audit")
	@RequiresPermissions(value = PermissionSet.Project.CODE_BASELINE_AUDIT)
    @RestLogAnnotation(describe = "知识包基线发布审核")
    public Result auditBaseline(KnowledgePackageBaseline knowledgePackageBaseline) {
        baselineService.auditBaseline(knowledgePackageBaseline);
        return Result.newSuccess();
    }

	/**
	 * 描述: 基线版本比较
	 * @param: [packageUuid, microUuid, baselineVersion]
	 * @author: 周庚新
	 * @date: 2020/7/6
	 * @return: com.beagledata.common.Result
	 *
	 */
	@GetMapping("compare")
	public Result compare(String packageUuid, Integer baselineV1, Integer baselineV2) {
		Result result=baselineService.compare(packageUuid, baselineV1, baselineV2);
		return result;
	}

    /**
     * @Author yangyongqiang
     * @Description 微服务部署上线用的查询表单接口
     * @Date 6:39 下午 2020/7/15
     **/
    @GetMapping("microBaseline")
    public Result selectListMicroBaseline(String packageUuid) {
        return Result.newSuccess().withData(baselineService.selectListMicroBaseline(packageUuid));
    }
}
