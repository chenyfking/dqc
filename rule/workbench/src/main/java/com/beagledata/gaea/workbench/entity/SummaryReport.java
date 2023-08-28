package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

import java.util.Date;

/**
 * 汇总报表
 *
 * Created by liulu on 2020/11/11.
 */
public class SummaryReport extends BaseEntity {
    private static final long serialVersionUID = -7138727352008128924L;

    /**
     * 机构uuid
     */
    private String orgUuid;
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 规则数量
     */
    private Long ruleCount;
    /**
     * 调用次数
     */
    private Long reqCount;
    /**
     * 批量调用次数
     */
    private Long batchReqCount;
    /**
     * 批量调用正确次数
     */
    private Long batchReqSuccessCount;
    /**
     * 联机调用次数
     */
    private Long onlineReqCount;
    /**
     * 联机调用正确次数
     */
    private Long onlineReqSuccessCount;
    /**
     * 统计日期
     */
    private Date statDate;
    /**
     * 联机调用正确率
     */
    private String onlineReqSuccessRatio;
    /**
     * 批量调用正确率
     */
    private String batchReqSuccessRatio;
    /**
     * 开始时间
     */
    private String startDate;
    /**
     * 结束时间
     */
    private String endDate;

    public String getOrgUuid() {
        return orgUuid;
    }

    public void setOrgUuid(String orgUuid) {
        this.orgUuid = orgUuid;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Long getRuleCount() {
        return ruleCount;
    }

    public void setRuleCount(Long ruleCount) {
        this.ruleCount = ruleCount;
    }

    public Long getReqCount() {
        return reqCount;
    }

    public void setReqCount(Long reqCount) {
        this.reqCount = reqCount;
    }

    public Long getBatchReqCount() {
        return batchReqCount;
    }

    public void setBatchReqCount(Long batchReqCount) {
        this.batchReqCount = batchReqCount;
    }

    public Long getBatchReqSuccessCount() {
        return batchReqSuccessCount;
    }

    public void setBatchReqSuccessCount(Long batchReqSuccessCount) {
        this.batchReqSuccessCount = batchReqSuccessCount;
    }

    public Long getOnlineReqCount() {
        return onlineReqCount;
    }

    public void setOnlineReqCount(Long onlineReqCount) {
        this.onlineReqCount = onlineReqCount;
    }

    public Long getOnlineReqSuccessCount() {
        return onlineReqSuccessCount;
    }

    public void setOnlineReqSuccessCount(Long onlineReqSuccessCount) {
        this.onlineReqSuccessCount = onlineReqSuccessCount;
    }

    public Date getStatDate() {
        return statDate;
    }

    public void setStatDate(Date statDate) {
        this.statDate = statDate;
    }

    public String getOnlineReqSuccessRatio() {
        return onlineReqSuccessRatio;
    }

    public void setOnlineReqSuccessRatio(String onlineReqSuccessRatio) {
        this.onlineReqSuccessRatio = onlineReqSuccessRatio;
    }

    public String getBatchReqSuccessRatio() {
        return batchReqSuccessRatio;
    }

    public void setBatchReqSuccessRatio(String batchReqSuccessRatio) {
        this.batchReqSuccessRatio = batchReqSuccessRatio;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SummaryReport{");
        sb.append("orgUuid='").append(orgUuid).append('\'');
        sb.append(", orgName='").append(orgName).append('\'');
        sb.append(", ruleCount=").append(ruleCount);
        sb.append(", reqCount=").append(reqCount);
        sb.append(", batchReqCount=").append(batchReqCount);
        sb.append(", batchReqSuccessCount=").append(batchReqSuccessCount);
        sb.append(", onlineReqCount=").append(onlineReqCount);
        sb.append(", onlineReqSuccessCount=").append(onlineReqSuccessCount);
        sb.append(", statDate=").append(statDate);
        sb.append(", onlineReqSuccessRatio='").append(onlineReqSuccessRatio).append('\'');
        sb.append(", batchReqSuccessRatio='").append(batchReqSuccessRatio).append('\'');
        sb.append(", startDate='").append(startDate).append('\'');
        sb.append(", endDate='").append(endDate).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
