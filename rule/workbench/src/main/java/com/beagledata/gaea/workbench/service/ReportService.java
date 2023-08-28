package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.vo.ReportSearchVO;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by liulu on 2020/11/11.
 */
public interface ReportService {
    /**
     * 查询汇总报表
     *
     * @param reportSearchVO
     * @return
     */
    Result listSummaryReport(ReportSearchVO reportSearchVO);

    /**
     * 查询明细报表
     *
     * @param reportSearchVO
     * @return
     */
    Result listDetailReport(ReportSearchVO reportSearchVO);

    /**
     * 查询审批类报表
     *
     * @param reportSearchVO
     * @return
     */
    Result listApprovalReport(ReportSearchVO reportSearchVO);

    /**
     * 查询上线报表
     *
     * @param reportSearchVO
     * @return
     */
    Result listDeploymentReport(ReportSearchVO reportSearchVO);

    /**
     * 预统计汇总报表
     */
    void preStatSummaryReport();

    /**
     * 预统计详细报表
     */
    void preStatDetailReport();

    /**
     * 预统计审批类报表
     */
    void preStatApprovalReport();

    /**
     * 预统计上线报表
     */
    void preStatDeploymentReport();

    /**
     * 导出报表
     *
     * @param reportSearchVO
     * @param response
     * @param type
     */
    void export(ReportSearchVO reportSearchVO, HttpServletResponse response, String type);
}
