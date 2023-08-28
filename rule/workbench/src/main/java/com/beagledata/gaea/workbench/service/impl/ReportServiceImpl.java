package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.*;
import com.beagledata.gaea.workbench.mapper.*;
import com.beagledata.gaea.workbench.service.ReportService;
import com.beagledata.gaea.workbench.util.PoiUtil;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.gaea.workbench.vo.ReportSearchVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReportServiceImpl implements ReportService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SummaryReportMapper summaryReportMapper;
    @Autowired
    private DetailReportMapper detailReportMapper;
    @Autowired
    private ApprovalReportMapper approvalReportMapper;
    @Autowired
    MicroMapper microMapper;
    @Autowired
    private DeploymentReportMapper deploymentReportMapper;

    Map<String, String> ruleCacheMap = new ConcurrentHashMap<String, String>();

    /**
     * 查询报表列表
     **/
    @Override
    public Result listSummaryReport (ReportSearchVO reportSearchVO) {
       if (reportSearchVO == null) {
           throw new IllegalArgumentException("传入参数不能为空！");
       }
       if (StringUtils.isNotBlank(reportSearchVO.getKeywords())) {
           reportSearchVO.setKeywords("%" + reportSearchVO.getKeywords() + "%");
       }

       if (reportSearchVO.getPage() <= 0) {
           reportSearchVO.setPage(1);
       }

       try {
           boolean isAdmin = UserHolder.hasAdminPermission();
           String userUuid = UserHolder.currentUserUuid();
           int count = summaryReportMapper.countTotal(reportSearchVO, isAdmin, userUuid);
           if (count <= 0) {
               return Result.emptyList();
           }
           int page = reportSearchVO.getPageNum() * (reportSearchVO.getPage() - 1);
           int pageNum = reportSearchVO.getPageNum();
           return Result.emptyList().withData(summaryReportMapper.selectSummaryReport(page, pageNum, reportSearchVO, isAdmin, userUuid)).withTotal(count);
       } catch (Exception e) {
            logger.error("查询汇总报表失败. reprotSearchVO: {}", reportSearchVO, e);
            return Result.emptyList();
       }
    }

    /**
     * 查询明细报表
     **/
    @Override
    public Result listDetailReport (ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("传入参数不能为空！");
        }
        if (StringUtils.isNotBlank(reportSearchVO.getKeywords())) {
            reportSearchVO.setKeywords("%" + reportSearchVO.getKeywords() + "%");
        }
        if (reportSearchVO.getPage() <= 0) {
            reportSearchVO.setPage(1);
        }

        try {
            boolean isAdmin = UserHolder.hasAdminPermission();
            String userUuid = UserHolder.currentUserUuid();
            int count = detailReportMapper.countTotal(reportSearchVO, isAdmin, userUuid);
            if (count <= 0) {
                return Result.emptyList();
            }
            int page = reportSearchVO.getPageNum() * (reportSearchVO.getPage() - 1);
            int pageNum = reportSearchVO.getPageNum();
            return Result.emptyList().withData(detailReportMapper.selectDetailReport(page, pageNum, reportSearchVO, isAdmin, userUuid)).withTotal(count);
        } catch (Exception e) {
            logger.error("查询明细报表失败. reporstSearchVO: {}", reportSearchVO, e);
            return Result.emptyList();
        }
    }

    /**
     * 查询模型优化报表
     **/
    @Override
    public Result listDeploymentReport (ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("传入参数不能为空！");
        }

        try {
            int count = deploymentReportMapper.countTotal(reportSearchVO);
            if (count <= 0) {
                return Result.emptyList();
            }
            int page = reportSearchVO.getPageNum() * (reportSearchVO.getPage() - 1);
            int pageNum = reportSearchVO.getPageNum();
            return Result.emptyList().withData(deploymentReportMapper.selectDeploymentReport(page, pageNum, reportSearchVO)).withTotal(count);
        } catch (Exception e) {
            logger.error("查询模型优化报表失败. reportSearchVO: {}", reportSearchVO, e);
            return Result.emptyList();
        }
    }

    /**
     * 查询审批类报表
     */
    @Override
    public Result listApprovalReport (ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("%" + reportSearchVO.getKeywords() + "%");
        }

        if (reportSearchVO.getPage() <= 0) {
            reportSearchVO.setPage(1);
        }
        try {
            boolean isAdmin = UserHolder.hasAdminPermission();
            String userUuid = UserHolder.currentUserUuid();
            int count = approvalReportMapper.countTotal(reportSearchVO, isAdmin, userUuid);
            if (count <= 0) {
                return Result.emptyList();
            }
            int page = reportSearchVO.getPageNum() * (reportSearchVO.getPage() - 1);
            int pageNum = reportSearchVO.getPageNum();
            return Result.emptyList().withData(approvalReportMapper.selectApprovalReport(page, pageNum, reportSearchVO, isAdmin, userUuid));
        } catch (Exception e) {
            logger.error("查询审批报表失败. reportSearchVO: {}", reportSearchVO, e);
            return Result.emptyList();
        }
    }

    @Override
    @Transactional
    public void preStatSummaryReport() {
        //查询最新时间的统计
        Date leastDate = summaryReportMapper.getLeastDate();
        if (leastDate == null) {
            leastDate = getDateStart(DateUtils.addDays(new Date(), -30));
        }
        Date yestodayDate = getDateEnd(DateUtils.addDays(new Date(), -1));
        Date statDate = DateUtils.addDays(leastDate, 1);
        //循环统计到当前日期的前一天
        while (statDate.before(yestodayDate)) {
            //统计结果入库
            //获取 统计日期（statDate）所属的年月
            String logTableMonth = com.beagledata.util.DateUtils.format(statDate, "yyyyMM");
            Date startDate = getDateStart(statDate);
            Date endDate = getDateEnd(startDate);
            List<SummaryReport> summaryReports = summaryReportMapper.preStat(startDate, endDate, logTableMonth);
            if (CollectionUtils.isNotEmpty(summaryReports)) {
                try {
                    //批量入库
                    summaryReportMapper.insertBatch(summaryReports);
                } catch (Exception e) {
                    //批量入库报Duplicate异常侧循环单条入库
                    if (e.getLocalizedMessage().contains("Duplicate")) {
                        for (SummaryReport summaryReport : summaryReports) {
                            try {
                                summaryReportMapper.insert(summaryReport);
                            } catch (Exception e1) {
                                if (!e.getLocalizedMessage().contains("Duplicate")) {
                                    logger.error("preStatSummaryReport error.", e);
                                    throw e;
                                }
                            }
                        }
                    } else {
                        logger.error("preStatSummaryReport error.", e);
                        throw e;
                    }
                }
            }
            // 统计日期加1
            statDate = DateUtils.addDays(statDate, 1);
        }
    }

    @Override
    public void preStatDetailReport() {
        //查询最新时间的统计
        Date leastDate = detailReportMapper.getLeastDate();
        if (leastDate == null) {
            leastDate = getDateStart(DateUtils.addDays(new Date(), -30));
        }
        Date yestodayDate = getDateEnd(DateUtils.addDays(new Date(), -1));
        Date statDate = DateUtils.addDays(leastDate, 1);
        while (statDate.before(yestodayDate)) {
            //循环到当前日期的前一天
            //统计结果入库
            //todo日志后期分表需要修改此处逻辑 获取 统计日期（statDate）所属的年月，去相应的表获取
            //获取统计的日志表名称
            String logTableMonth = com.beagledata.util.DateUtils.format(statDate, "yyyyMM");
            Date starDate = getDateStart(statDate);
            Date endDate = getDateEnd(statDate);
            List<DetailReport> detailReports = detailReportMapper.preStat(starDate, endDate, logTableMonth);
            if (CollectionUtils.isNotEmpty(detailReports)) {
                List<DetailReport> insertReports = new ArrayList<DetailReport>();
                for (DetailReport detailReport : detailReports) {
                    String name = getRuleName(detailReport.getRuleUuid());
                    if (StringUtils.isNotBlank(name)) {
                        detailReport.setRuleName(name);
                        insertReports.add(detailReport);
                    }
                }
                if (CollectionUtils.isEmpty(insertReports)) {
                    statDate = DateUtils.addDays(statDate, 1);
                    continue;
                }
                try {
                    detailReportMapper.insertBatch(insertReports);
                } catch (Exception e) {
                    if (e.getLocalizedMessage().contains("Duplicate")) {
                        for (DetailReport detailReport : detailReports) {
                            try {
                                detailReportMapper.insert(detailReport);
                            } catch (Exception e1) {
                                if (e.getLocalizedMessage().contains("Duplicate")) {
                                    logger.error("preStatDetailReport error.", e);
                                    throw e;
                                }
                            }
                        }
                    } else {
                        logger.error("preStatDetailReport error.", e);
                        throw e;
                    }
                }
            }
            //统计日期加1
            statDate = DateUtils.addDays(statDate, 1);
        }
    }

    @Override
    public void preStatApprovalReport() {
        //查询最新时间的统计
        Date leasDate = approvalReportMapper.getLeastDate();
        if (leasDate == null) {
            leasDate = getDateStart(DateUtils.addDays(new Date(), -30));
        }
        Date yestodayDate = getDateEnd(DateUtils.addDays(new Date(), -1));
        Date statDate = DateUtils.addDays(leasDate, 1);
        while (statDate.before(yestodayDate)) {
            //循环统计到当前日期的前一天
            //统计结果入库
            //todo 日志日期分表需要修改此处逻辑 获取 统计日期（statDate）所属的年月，去相应的表获取
            //获取统计的日志表名称
            String logTableMonth = com.beagledata.util.DateUtils.format(statDate, "yyyyMM");
            Date startDate = getDateStart(statDate);
            Date endDate = getDateEnd(startDate);
            List<ApprovalReport> approvalReports = approvalReportMapper.preStat(startDate, endDate, logTableMonth);
            if (CollectionUtils.isNotEmpty(approvalReports)) {
                List<ApprovalReport> insertReports = new ArrayList<ApprovalReport>();
                for (ApprovalReport approvalReport : approvalReports) {
                    String name = getRuleName(approvalReport.getRuleUuid());
                    if (StringUtils.isNotBlank(name)) {
                        approvalReport.setRuleName(name);
                        insertReports.add(approvalReport);
                    }
                }
                if (CollectionUtils.isEmpty(insertReports)) {
                    startDate = DateUtils.addDays(startDate, 1);
                    continue;
                }
                try {
                    approvalReportMapper.insertBatch(insertReports);
                } catch (Exception e) {
                    if (e.getLocalizedMessage().contains("Duplicate")) {
                        for (ApprovalReport approvalReport : approvalReports) {
                            try {
                                approvalReportMapper.insert(approvalReport);
                            } catch (Exception e1) {
                                if (!e.getLocalizedMessage().contains("Duplicate")) {
                                    logger.error("preStatApprovalReport error.", e);
                                    throw e;
                                }
                            }
                        }
                    } else {
                        logger.error("preStatApprovalReport error.", e);
                        throw e;
                    }
                }
            }
            //统计日期加1
            statDate = DateUtils.addDays(startDate, 1);
        }
    }

    @Override
    public void preStatDeploymentReport() {
        //查询最新时间的统计
        Date leastDate = deploymentReportMapper.getLeastDate();
        if (leastDate == null) {
            leastDate = getDateStart(DateUtils.addDays(new Date(), -30));
        }
        Date yestodayDate = getDateEnd(DateUtils.addDays(new Date(), -1));
        Date statDate = DateUtils.addDays(leastDate, 1);
        while (statDate.before(yestodayDate)) {
            //循环统计到当前日期的前一天
            //统计结果入库
            //todo 日志后期分表需要需要此处逻辑 获取 统计日期（statDate）所属的年月，去相应的表获取
            //获取统计的日志表名称
            String logTableMonth = com.beagledata.util.DateUtils.format(statDate, "yyyyMM");
            Date startDate = getDateStart(statDate);
            Date endDate = getDateEnd(startDate);
            List<DeploymentReport> deploymentReports = deploymentReportMapper.preStat(startDate, endDate, logTableMonth);
            if (CollectionUtils.isNotEmpty(deploymentReports)) {
                try {
                    deploymentReportMapper.insertBatch(deploymentReports);
                } catch (Exception e) {
                    if (e.getLocalizedMessage().contains("Duplicate")) {
                        for (DeploymentReport deploymentReport : deploymentReports) {
                            try {
                                deploymentReportMapper.insert(deploymentReport);
                            } catch (Exception e1) {
                                if (!e.getLocalizedMessage().contains("Duplicate")) {
                                    logger.error("preStatDeployment error.", e);
                                    throw e;
                                }
                            }
                        }
                    } else {
                        logger.error("preStartDeploymentReport error.", e);
                        throw e;
                    }
                }
            }
            //统计日期加1
            statDate = DateUtils.addDays(startDate, 1);
        }
    }

    /**
     * 获取日期的结束时间
     *
     * @return
     */
    private Date getDateEnd (Date date) {
        date = DateUtils.setHours(date, 23);
        date = DateUtils.setMinutes(date, 59);
        date = DateUtils.setSeconds(date, 59);
        return date;
    }

    /**
     * 获取日期的开始时间
     *
     * @return
     */
    private Date getDateStart(Date date) {
        date = DateUtils.setHours(date, 0);
        date = DateUtils.setMinutes(date, 0);
        date = DateUtils.setSeconds(date, 0);
        return date;
    }

    /**
     * 根据规则（实际为服务）uuid获取规则（服务）名称
     * @param ruleUuid
     * @return
     */
    private String getRuleName(String ruleUuid) {
        String ruleName = ruleCacheMap.get(ruleUuid);
        if (StringUtils.isNotEmpty(ruleName)) {
            return ruleName;
        }
        Micro micro = microMapper.selectByUuid(ruleUuid);
        if (micro != null) {
            ruleName = micro.getName();
            ruleCacheMap.put(ruleUuid, ruleName);
        }
        return ruleName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ReportServiceImpl [logger=");
        builder.append(logger);
        builder.append(", summaryReportMapper=");
        builder.append(summaryReportMapper);
        builder.append(", detailReportMapper=");
        builder.append(detailReportMapper);
        builder.append(", approvalReportMapper");
        builder.append(approvalReportMapper);
        builder.append(", microMapper");
        builder.append(microMapper);
        builder.append(", ruleCacheMap");
        builder.append(ruleCacheMap);
        builder.append("]");
        return builder.toString();
    }

    /**
     * 导出汇总报表csv文件
     */
    public List<SummaryReport> summaryAll (ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("传入参数不能为空！");
        }
        if (StringUtils.isNotBlank(reportSearchVO.getKeywords())) {
            reportSearchVO.setKeywords("%" + reportSearchVO.getKeywords() + "%");
        }
        try {
            boolean isAdmin = UserHolder.hasAdminPermission();
            String userUuid = UserHolder.currentUserUuid();
            return summaryReportMapper.selectSummaryReport(null, null, reportSearchVO, isAdmin, userUuid);
        } catch (Exception e) {
            logger.error("detailAll error. reporSearchVO: {}", reportSearchVO, e);
            return Collections.emptyList();
        }
    }

    /**
     * 导出明细报表CSV文件
     */
    public List<DetailReport> detailAll (ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("传入参数不能为空！");
        }
        if (StringUtils.isNotBlank(reportSearchVO.getKeywords())) {
            reportSearchVO.setKeywords("%" + reportSearchVO.getKeywords() + "%");
        }
        try {
            boolean isAdmin = UserHolder.hasAdminPermission();
            String userUuid = UserHolder.currentUserUuid();
            return detailReportMapper.selectDetailReport(null, null, reportSearchVO, isAdmin, userUuid);
        } catch (Exception e) {
            logger.error("detailAll error. reportSearchVO: {}", reportSearchVO, e);
            return Collections.emptyList();
        }
    }

    /**
     * 导出模型优化报表CSV文件
     */
    public List<DeploymentReport> deploymentAll(ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("传入参数不能为空！");
        }
        try {
            return deploymentReportMapper.selectDeploymentReport(null, null, reportSearchVO);
        } catch (Exception e) {
            logger.error("deploymentAll error. reportSearchVO: {}", reportSearchVO, e);
            return (List<DeploymentReport>) Result.emptyList();
        }
    }

    /**
     * 导出审批类报表CSV文件
     */
    public List<ApprovalReport> approvalAll(ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("传入参数不能为空！");
        }
        if (StringUtils.isNotBlank(reportSearchVO.getKeywords())) {
            reportSearchVO.setKeywords("%" + reportSearchVO.getKeywords() + "%");
        }
        try {
            boolean isAdmin = UserHolder.hasAdminPermission();
            String userUuid = UserHolder.currentUserUuid();
            return approvalReportMapper.selectApprovalReport(null, null, reportSearchVO, isAdmin, userUuid);
        } catch (Exception e) {
            logger.error("approvalAll error. reportSearchVO: {}", reportSearchVO, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询审批类报表日期
     *
     **/
    public ApprovalReport selectApprovalReportDate(ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("传入参数不能为空");
        }
        if (StringUtils.isNotBlank(reportSearchVO.getKeywords())) {
            reportSearchVO.setKeywords("%" + reportSearchVO.getKeywords() + "%");
        }
        try {
            boolean isAdmin = UserHolder.hasAdminPermission();
            String userUuid = UserHolder.currentUserUuid();
            return approvalReportMapper.selectApprovalReportDate(reportSearchVO, isAdmin, userUuid);
        } catch (Exception e) {
            logger.error("selectApprovalReportDate error. reportSearchVO: {}", reportSearchVO, e);
            return null;
        }
    }

    /**
     * 查询明细报表日期
     **/
    public DetailReport selectDetailReportDate(ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("传入参数不能为空！");
        }
        if (StringUtils.isNotBlank(reportSearchVO.getKeywords())) {
            reportSearchVO.setKeywords("%" + reportSearchVO.getKeywords() + "%");
        }
        try {
            boolean isAdmin = UserHolder.hasAdminPermission();
            String userUuid = UserHolder.currentUserUuid();
            return detailReportMapper.selectDetailReportDate(reportSearchVO, isAdmin, userUuid);
        } catch (Exception e) {
            logger.error("selectDetailReportDate error. reportSearchVO: {}", reportSearchVO, e);
            return null;
        }
    }

    /**
     * 查询模型优化日期
     **/
    public DeploymentReport selectDeploymentReportDate(ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("传入参数不能为空！");
        }
        try {
            return deploymentReportMapper.selectDeploymentReportDate(reportSearchVO);
        } catch (Exception e) {
            logger.error("查询模型优化报表日期失败. reportSearchVO: {}", reportSearchVO, e);
            return null;
        }
    }

    /**
     * 查询日期
     **/
    public SummaryReport selectSummaryReportDate(ReportSearchVO reportSearchVO) {
        if (reportSearchVO == null) {
            throw new IllegalArgumentException("传入参数不能为空！");
        }
        if (StringUtils.isNotBlank(reportSearchVO.getKeywords())) {
            reportSearchVO.setKeywords("%" + reportSearchVO.getKeywords() + "%");
        }
        try {
            boolean isAdmin = UserHolder.hasAdminPermission();
            String userUuid = UserHolder.currentUserUuid();
            return summaryReportMapper.selectSummaryReportDate(reportSearchVO, isAdmin, userUuid);
        } catch (Exception e) {
            logger.error("selectSummaryReportDate error. reportSearchVO: {}", reportSearchVO, e);
            return null;
        }
    }

    @Override
    public void export(ReportSearchVO reportSearchVO, HttpServletResponse response, String type) {
        List<List<String>> list = null;
        if ("summary".equals(type)) {
            list = exportSummary(reportSearchVO);
        } else if ("approval".equals(type)) {
            list = exportApproval(reportSearchVO);
        } else if ("detail".equals(type)) {
            list = exportDetail(reportSearchVO);
        } else if ("deployment".equals(type)) {
            list = exportDeployment(reportSearchVO);
        }

        try {
            PoiUtil.downExcel(list, false, response);
        } catch (Exception e) {
            logger.error("项目下载失败: {}", reportSearchVO, e);
            throw new IllegalArgumentException("下载失败");
        }
    }

    private List<List<String>> exportDeployment(ReportSearchVO reportSearchVO) {
        String startDate = "";
        String endDate = "";
        List<List<String>> list = new ArrayList<List<String>>();
        List<DeploymentReport> deploymentAll = deploymentAll(reportSearchVO);

        if (StringUtils.isBlank(reportSearchVO.getEndDate()) || StringUtils.isBlank(reportSearchVO.getStartDate())) {
            DeploymentReport selectDeploymentReportDate = selectDeploymentReportDate(reportSearchVO);
            if (selectDeploymentReportDate != null) {
                startDate = selectDeploymentReportDate.getStartDate();
                endDate = selectDeploymentReportDate.getStartDate();
                if (StringUtils.isNotBlank(reportSearchVO.getStartDate())) {
                    startDate = reportSearchVO.getStartDate();
                }
                if (StringUtils.isNotBlank(reportSearchVO.getEndDate())) {
                    endDate = reportSearchVO.getEndDate();
                }
            }
        } else {
            startDate = reportSearchVO.getStartDate();
            endDate = reportSearchVO.getEndDate();
        }
        List<String> title = new ArrayList<String>();
        title.add("模型优化报表(" + startDate + " 至 " + endDate + ")");
        list.add(title);
        List<String> titleDate = new ArrayList<String>();
        titleDate.add("模型");
        titleDate.add("调用次数");
        titleDate.add("批量调用次数");
        titleDate.add("批量调用正确率");
        titleDate.add("联机调用次数");
        titleDate.add("联机调用正确率");
        titleDate.add("调用通过率");
        list.add(titleDate);
        if (deploymentAll != null) {
            for (DeploymentReport deployment : deploymentAll) {
                List<String> coList = new ArrayList<String>();
                String baseline = "V" + deployment.getPkgBaseline();
                Long reqCount = deployment.getReqCount();
                Long batchReqCount = deployment.getBatchReqCount();
                String batchReqSuccessRatio = deployment.getOnlineReqSuccessRatio();
                Long onlineReqCount = deployment.getOnlineReqCount();
                String onlineReqSuccessRation =  deployment.getOnlineReqSuccessRatio();
                String reqPassSuccessRatio = deployment.getReqPassSuccessRatio();

                coList.add(baseline == null ? "" : baseline);
                coList.add(reqCount == null ? "" : reqCount + "");
                coList.add(batchReqCount == null ? "" : batchReqCount + "");
                coList.add(batchReqSuccessRatio == null ? "" : batchReqSuccessRatio + "%");
                coList.add(onlineReqCount == null ? "" : onlineReqCount + "");
                coList.add(onlineReqSuccessRation == null ? "" : onlineReqSuccessRation + "%");
                coList.add(reqPassSuccessRatio == null ? "" : reqPassSuccessRatio + "%");
                list.add(coList);
            }
        }
        return list;
    }

    private List<List<String>> exportDetail(ReportSearchVO reportSearchVO) {
        String startDate = "";
        String endDate = "";
        String keyword = reportSearchVO.getKeywords();
        List<List<String>> list = new ArrayList<List<String>>();
        List<DetailReport> detailAll = detailAll(reportSearchVO);

        if (StringUtils.isBlank(reportSearchVO.getEndDate()) || StringUtils.isBlank(reportSearchVO.getStartDate())) {
            DetailReport selectDetailReportDate = selectDetailReportDate(reportSearchVO);
            if (selectDetailReportDate != null) {
                startDate = selectDetailReportDate.getStartDate();
                endDate = selectDetailReportDate.getEndDate();
                if (StringUtils.isNotBlank(reportSearchVO.getStartDate())) {
                    startDate = reportSearchVO.getStartDate();
                }
                if (StringUtils.isNotBlank(reportSearchVO.getEndDate())) {
                    endDate = reportSearchVO.getEndDate();
                }
            }
        } else {
            startDate = reportSearchVO.getStartDate();
            endDate = reportSearchVO.getEndDate();
        }
        List<String> title = new ArrayList<String>();
        title.add("明细报表（" + startDate + " 至 " + endDate + "）关键词: " + keyword);
        list.add(title);
        List<String> titleDate = new ArrayList<String>();
        titleDate.add("规则名称");
        titleDate.add("归属用户");
        titleDate.add("归属部门");
        titleDate.add("调用次数");
        titleDate.add("批量调用次数");
        titleDate.add("批量调用正确率");
        titleDate.add("联机调用次数");
        titleDate.add("联机调用正确率");
        list.add(titleDate);
        if (detailAll != null) {
            for (DetailReport detail : detailAll) {
                List<String> coList = new ArrayList<String>();
                String ruleName = detail.getRuleName();
                String ownerUserName = detail.getOwnerUserName();
                String ownerOrgName = detail.getOwnerOrgName();
                Long reqCount = detail.getReqCount();
                Long batchReqCount = detail.getBatchReqCount();
                String batchReqSuccessRatio = detail.getBatchReqSuccessRatio();
                Long onlineReqCount = detail.getOnlineReqCount();
                String onlineReqSuccessRatio = detail.getOnlineReqSuccessRatio();

                coList.add(ruleName == null ? "" : ruleName);
                coList.add(ownerUserName == null ? "" : ownerUserName);
                coList.add(ownerOrgName == null ? "" : ownerOrgName);
                coList.add(reqCount == null ? "" : reqCount + "");
                coList.add(batchReqCount == null ? "" : batchReqCount + "");
                coList.add(batchReqSuccessRatio == null ? "" : batchReqSuccessRatio + "%");
                coList.add(onlineReqCount == null ? "" : onlineReqCount + "");
                coList.add(onlineReqSuccessRatio == null ? "" : onlineReqSuccessRatio + "%");
                list.add(coList);
            }
        }
        return list;
    }

    private List<List<String>> exportApproval(ReportSearchVO reportSearchVO) {
        String startDate = "";
        String endDate = "";
        String keyword = reportSearchVO.getKeywords();
        List<ApprovalReport> approvalAll = approvalAll(reportSearchVO);
        List<List<String>> list = new ArrayList<List<String>>();

        if (StringUtils.isBlank(reportSearchVO.getEndDate()) || StringUtils.isBlank(reportSearchVO.getStartDate())) {
            ApprovalReport selectApprovalReportDate = selectApprovalReportDate(reportSearchVO);
            if (selectApprovalReportDate != null) {
                startDate = selectApprovalReportDate.getStartDate();
                endDate = selectApprovalReportDate.getEndDate();
                if (StringUtils.isNotBlank(reportSearchVO.getStartDate())) {
                    startDate = reportSearchVO.getStartDate();
                }
                if (StringUtils.isNotBlank(reportSearchVO.getEndDate())) {
                    endDate = reportSearchVO.getEndDate();
                }
            }
        } else {
            startDate = reportSearchVO.getStartDate();
            endDate = reportSearchVO.getEndDate();
        }
        List<String> title = new ArrayList<String>();
        title.add("审批类报表（" + startDate + " 至 " + endDate + "）关键词： " + keyword);
        list.add(title);
        List<String> titleDate = new ArrayList<String>();
        titleDate.add("规则名称");
        titleDate.add("归属用户");
        titleDate.add("归属部门");
        titleDate.add("调用次数");
        titleDate.add("调用通过率");
        list.add(titleDate);
        if (approvalAll != null) {
            for (ApprovalReport approval : approvalAll) {
                List<String> coList = new ArrayList<String>();
                String ruleName = approval.getRuleName();
                String ownerUserName = approval.getRuleName();
                String ownerOrgName = approval.getOwnerOrgName();
                Long batchReqCount = approval.getReqCount();
                String reqPassCountRatio = approval.getReqPassCountRatio();

                coList.add(ruleName == null ? "" : ruleName);
                coList.add(ownerUserName == null ? "" : ownerUserName);
                coList.add(ownerOrgName == null ? "" : ownerOrgName);
                coList.add(batchReqCount == null ? "" : batchReqCount + "");
                coList.add(reqPassCountRatio == null ? "" : reqPassCountRatio + "%");
                list.add(coList);
            }
        }
        return list;
    }

    private List<List<String>> exportSummary(ReportSearchVO reportSearchVO) {
        String startDate = "";
        String endDate = "";
        String keyword = reportSearchVO.getKeywords();
        List<SummaryReport> summaryAll = summaryAll(reportSearchVO);
        List<List<String>> list = new ArrayList<List<String>>();

        if (StringUtils.isBlank(reportSearchVO.getEndDate()) || StringUtils.isBlank(reportSearchVO.getStartDate())) {
            SummaryReport selectSummaryReporDate = selectSummaryReportDate(reportSearchVO);
            if (selectSummaryReporDate != null) {
                startDate = selectSummaryReporDate.getStartDate();
                endDate = selectSummaryReporDate.getEndDate();
                if (StringUtils.isNotBlank(reportSearchVO.getStartDate())) {
                    startDate = reportSearchVO.getStartDate();
                }
                if (StringUtils.isNotBlank(reportSearchVO.getEndDate())) {
                    endDate = reportSearchVO.getEndDate();
                }
            }
        } else {
            startDate = reportSearchVO.getStartDate();
            endDate = reportSearchVO.getEndDate();
        }
        List<String> title = new ArrayList<String>();
        title.add("汇总报表（" + startDate + " 至 " + endDate + ")机构名称：" + keyword);
        list.add(title);
        List<String> titleDate = new ArrayList<String>();
        titleDate.add("机构名称");
        titleDate.add("规则数量");
        titleDate.add("调用次数");
        titleDate.add("批量调用次数");
        titleDate.add("批量调用正确率");
        titleDate.add("联机调用次数");
        titleDate.add("联机调用正确率");
        list.add(titleDate);
        if (summaryAll != null) {
            for (SummaryReport summary : summaryAll) {
                List<String> coList = new ArrayList<String>();
                String name = summary.getOrgName();
                Long ruleCount = summary.getRuleCount();
                Long reqCount = summary.getReqCount();
                Long batchReqCount = summary.getBatchReqCount();
                String batchReqSuccessRatio = summary.getBatchReqSuccessRatio();
                Long onlineReqCount = summary.getOnlineReqCount();
                String onlineReqSuccessRatio = summary.getOnlineReqSuccessRatio();

                coList.add(name == null ? "" : name);
                coList.add(ruleCount == null ? "" : ruleCount + "");
                coList.add(reqCount == null ? "" : reqCount + "");
                coList.add(batchReqCount == null ? "" : batchReqCount + "");
                coList.add(batchReqSuccessRatio == null ? "" : batchReqSuccessRatio + "%");
                coList.add(onlineReqCount == null ? "" : onlineReqCount + "");
                coList.add(onlineReqSuccessRatio == null ? "" : onlineReqSuccessRatio + "%");
                list.add(coList);
            }
        }
        return list;
    }
}