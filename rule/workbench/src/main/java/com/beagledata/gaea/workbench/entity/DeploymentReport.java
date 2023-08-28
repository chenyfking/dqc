package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

import java.util.Date;

/**
 * 上线报表
 *
 * Created by liulu on 2020/11/11.
 */
public class DeploymentReport extends BaseEntity {
    private static final long serialVersionUID = 5184178567362955145L;

    /**
     * 上线uiud
     */
    private String deployUuid;
    /**
     * 知识包基线
     */
    private Integer pkgBaseline;
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
     * 通过总数
     */
    private Long reqPassCount;
    /**
     * 联机调用正确率
     */
    private String onlineReqSuccessRatio;
    /**
     * 批量调用正确率
     */
    private String batchReqSuccessRatio;
    /**
     * 调用通过率
     */
    private String reqPassSuccessRatio;
    /**
     * 开始时间
     */
    private String startDate;
    /**
     * 结束时间
     */
    private String endDate;

    public String getDeployUuid() {
        return deployUuid;
    }

    public void setDeployUuid(String deployUuid) {
        this.deployUuid = deployUuid;
    }

    public Integer getPkgBaseline() {
        return pkgBaseline;
    }

    public void setPkgBaseline(Integer pkgBaseline) {
        this.pkgBaseline = pkgBaseline;
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

    public Long getReqPassCount() {
        return reqPassCount;
    }

    public void setReqPassCount(Long reqPassCount) {
        this.reqPassCount = reqPassCount;
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

    public String getReqPassSuccessRatio() {
        return reqPassSuccessRatio;
    }

    public void setReqPassSuccessRatio(String reqPassSuccessRatio) {
        this.reqPassSuccessRatio = reqPassSuccessRatio;
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
        StringBuilder sb = new StringBuilder("DeploymentReport{");
        sb.append("deployUuid='").append(deployUuid).append('\'');
        sb.append(", pkgBaseline=").append(pkgBaseline);
        sb.append(", reqCount=").append(reqCount);
        sb.append(", batchReqCount=").append(batchReqCount);
        sb.append(", batchReqSuccessCount=").append(batchReqSuccessCount);
        sb.append(", onlineReqCount=").append(onlineReqCount);
        sb.append(", onlineReqSuccessCount=").append(onlineReqSuccessCount);
        sb.append(", statDate=").append(statDate);
        sb.append(", reqPassCount=").append(reqPassCount);
        sb.append(", onlineReqSuccessRatio='").append(onlineReqSuccessRatio).append('\'');
        sb.append(", batchReqSuccessRatio='").append(batchReqSuccessRatio).append('\'');
        sb.append(", reqPassSuccessRatio='").append(reqPassSuccessRatio).append('\'');
        sb.append(", startDate='").append(startDate).append('\'');
        sb.append(", endDate='").append(endDate).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
