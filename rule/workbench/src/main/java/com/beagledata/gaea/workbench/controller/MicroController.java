package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.Micro;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.MicroService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 服务操作
 * Created by Chenyafeng on 2018/12/4.
 */
@RestController
@RequestMapping("micro")
public class MicroController {
    @Autowired
    private MicroService microService;

    /**
     * 多条件查询服务列表
     * @author chenyafeng
     * @date 2019/4/1
     */
    @GetMapping("")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_MICRO_VIEW)
    public Result listPage(@RequestParam(required = false, defaultValue = "1") int page,
                                     @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum,
                                     Micro micro) {
        return microService.listPage(micro, page, pageNum);
    }

    /**
     * 启用服务
     * @author chenyafeng
     * @date 2018/12/4
     */
    @PostMapping("enable")
    public Result enable(String uuid) {
        return microService.enableMicro(uuid, true);
    }

    /**
     * 停用服务
     * @author chenyafeng
     * @date 2018/12/4
     */
    @PostMapping("disenable")
    public Result disenable(String uuid) {
        return microService.enableMicro(uuid, false);
    }


    /**
     * 更新服务
     * @author chenyafeng
     * @date 2018/12/4
     */
    @PostMapping("update")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_MICRO_EDIT)
    @RestLogAnnotation(describe = "编辑服务")
    public Result updateMicro(Micro micro) {
        return microService.updateMicro(micro);
    }

    /**
     * 删除服务
     * @author chenyafeng
     * @date 2018/12/4
     */
    @PostMapping("delete")
    @RestLogAnnotation(describe = "删除服务")
    public Result delete(String uuid) {
        microService.deleteMicro(uuid);
        return Result.newSuccess();
    }

    /**
     * 查看服务调用文档
     * Created by Chenyafeng on 2019/6/13
     */
    @GetMapping("apidoc")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_MICRO_APIDOC)
    public Result getScript(String uuid) {
        return microService.getApiDoc(uuid);
    }
    
    /**
     * 获取服务调用记录
     *
     * @param uuid
     * @return
     */
    @GetMapping("executerecords")
    public Result listExecuteRecords(@RequestParam(required = false, defaultValue = "1") int page,
                                     @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                     String uuid) {
        return microService.listPageExecuteRecords(uuid, page, pageNum, startTime, endTime);
    }

    @PostMapping("approval")
    @RestLogAnnotation(describe = "设置审批字段")
    @RequiresPermissions(PermissionSet.Micro.CODE_MICRO_EDIT)
    public Result approval(Micro micro) {
        return microService.updateMicro(micro);
    }

    @GetMapping("approvaldata")
    public Result approvalData(String uuid) {
        return microService.approvalData(uuid);
    }

    @GetMapping("baseline")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_MICRO_VIEW)
    public Result listBaseline(String microUuid) {
        return Result.newSuccess().withData(microService.listBaselineForDeploy(microUuid));
    }
}
