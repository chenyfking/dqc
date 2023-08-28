package com.beagledata.gaea.workbench.scheduled;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.common.task.AbstractTask;
import com.beagledata.gaea.workbench.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 预统计前一天的报表
 *
 * Created by liulu on 2020/11/11.
 */
public class PreStatReportTask extends AbstractTask {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ReportService reportService = SpringBeanHolder.getBean(ReportService.class);

    @Override
    public String desc() {
        return "预统计前一天的报表";
    }

    @Override
    public void run() {
        logger.info("开始预统计审批类报表");
        try {
            reportService.preStatApprovalReport();
        } catch (Exception e) {
            logger.error("预统计审批类报表失败", e);
        }
        logger.info("结束预统计审批类报表");

        logger.info("开始预统计明细报表");
        try {
            reportService.preStatDetailReport();
        } catch (Exception e) {
            logger.error("预统计明细报表失败", e);
        }
        logger.info("结束预统计明细报表");

        logger.info("开始预统计汇总报表");
        try {
            reportService.preStatSummaryReport();
        } catch (Exception e) {
            logger.error("预统计汇总报表失败", e);
        }
        logger.info("结束预统计汇总报表");

        logger.info("开始预统计上线决策报表");
        try {
            reportService.preStatDeploymentReport();
        } catch (Exception e) {
            logger.error("预统计上线决策报表失败", e);
        }
        logger.info("结束预统计上线决策报表");
    }
}
