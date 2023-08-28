package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.ReportService;
import com.beagledata.gaea.workbench.vo.ReportSearchVO;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by liulu on 2020/11/11.
 */
@RestController
@RequestMapping("report")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("summary")
    @RequiresPermissions(PermissionSet.Micro.CODE_REPORT_VIEW)
    public Result summary(ReportSearchVO reportSearchVO) {
        return reportService.listSummaryReport(reportSearchVO);
    }

    @GetMapping("approval")
    @RequiresPermissions(PermissionSet.Micro.CODE_REPORT_VIEW)
    public Result approval(ReportSearchVO reportSearchVO) {
        return reportService.listApprovalReport(reportSearchVO);
    }

    @GetMapping("detail")
    @RequiresPermissions(PermissionSet.Micro.CODE_REPORT_VIEW)
    public Result detail(ReportSearchVO reportSearchVO) {
        return reportService.listDetailReport(reportSearchVO);
    }

    @GetMapping("deployment")
    @RequiresPermissions(PermissionSet.Micro.CODE_REPORT_VIEW)
    public Result deployment(ReportSearchVO reportSearchVO) {
        return reportService.listDeploymentReport(reportSearchVO);
    }

    @GetMapping("exportSummary")
    @RequiresPermissions(PermissionSet.Micro.CODE_REPORT_EXPORT)
    @RestLogAnnotation(describe = "导出汇总报表")
    public void exportSummary(ReportSearchVO reportSearchVO, HttpServletResponse response) {
        reportService.export(reportSearchVO, response, "summary");
    }

    @GetMapping("exportApproval")
    @RequiresPermissions(PermissionSet.Micro.CODE_REPORT_EXPORT)
    @RestLogAnnotation(describe = "导出审批类报表")
    public void exportApproval(ReportSearchVO reportSearchVO, HttpServletResponse response) {
        reportService.export(reportSearchVO, response, "approval");
    }

    @GetMapping("exportDetail")
    @RequiresPermissions(PermissionSet.Micro.CODE_REPORT_EXPORT)
    @RestLogAnnotation(describe = "导出明细报表")
    public void exportDetail(ReportSearchVO reportSearchVO, HttpServletResponse response) {
        reportService.export(reportSearchVO, response, "detail");
    }

    @GetMapping("exportDeployment")
    @RequiresPermissions(PermissionSet.Micro.CODE_REPORT_EXPORT)
    @RestLogAnnotation(describe = "导出上线决策报表")
    public void exportDeployment(ReportSearchVO reportSearchVO, HttpServletResponse response) {
        reportService.export(reportSearchVO, response, "deployment");
    }
}
