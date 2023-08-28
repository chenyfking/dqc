package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.DecisionLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by liulu on 2020/11/11.
 */
@RestController
@RequestMapping("decision")
public class DecisionLogController {
    @Autowired
    private DecisionLogService decisionLogService;

    @GetMapping("logExport")
    public void export(HttpServletResponse response, Date startDate, Date endDate, String microUuid) {
        decisionLogService.downloadDecisionLog(microUuid, startDate, endDate, response);
    }

    @GetMapping("trace")
    @RequiresPermissions(PermissionSet.Micro.CODE_MICRO_TRACE)
    public Result trace(String seqNo) {
        return decisionLogService.trace(seqNo);
    }
}
