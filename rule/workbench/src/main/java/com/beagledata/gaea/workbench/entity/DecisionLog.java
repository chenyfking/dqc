package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * 决策日志
 *
 * Created by liulu on 2020/11/11.
 */
public class DecisionLog extends BaseEntity {
    private static final long serialVersionUID = 7986063444600889372L;

    public static final int REQ_TYPE_ONLINE = 0;
    public static final int REQ_TYPE_BATCH = 1;

    public static final int STATE_RUNNING = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_FAIL = 2;

    /**
     * 机构uuid
     */
    private String orgUuid;
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 归属用户uuid
     */
    private String userUuid;
    /**
     * 归属用户名称
     */
    private String userName;
    /**
     * 决策服务uuid
     */
    private String microUuid;
    /**
     * 上线uuid
     */
    private String deployUuid;
    /**
     * 知识包uuid
     */
    private String pkgUuid;
    /**
     * 知识包基线
     */
    private Integer pkgBaseline;
    /**
     * 请求类型，0：联机请求，1：批量调用
     */
    private Integer reqType;
    /**
     * 状态，0：执行中，1：执行成功，2：执行失败
     */
    private Integer state;
    /**
     * 审批通过标志，空：不是审批类，0：审批未通过，1：审批通过
     */
    private Boolean pass;
    /**
     * 生效的
     */
    private boolean effective;
    /**
     * 请求文本
     */
    private String reqText;
    /**
     * 响应文本
     */
    private String resText;
    /**
     * 响应全部文本
     */
    private String fullText;
    /**
     * 决策日志表所属月份
     */
    private String logTableMonth;
    /**
     * 决策轨迹
     */
    private String decisionTrace;

    /**
     * @return 日志流水号
     */
    public String getSeqNo() {
        return logTableMonth + getUuid();
    }

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

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMicroUuid() {
        return microUuid;
    }

    public void setMicroUuid(String microUuid) {
        this.microUuid = microUuid;
    }

    public String getDeployUuid() {
        return deployUuid;
    }

    public void setDeployUuid(String deployUuid) {
        this.deployUuid = deployUuid;
    }

    public String getPkgUuid() {
        return pkgUuid;
    }

    public void setPkgUuid(String pkgUuid) {
        this.pkgUuid = pkgUuid;
    }

    public Integer getPkgBaseline() {
        return pkgBaseline;
    }

    public void setPkgBaseline(Integer pkgBaseline) {
        this.pkgBaseline = pkgBaseline;
    }

    public Integer getReqType() {
        return reqType;
    }

    public void setReqType(Integer reqType) {
        this.reqType = reqType;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Boolean getPass() {
        return pass;
    }

    public void setPass(Boolean pass) {
        this.pass = pass;
    }

    public boolean isEffective() {
        return effective;
    }

    public void setEffective(boolean effective) {
        this.effective = effective;
    }

    public String getReqText() {
        return reqText;
    }

    public void setReqText(String reqText) {
        this.reqText = reqText;
    }

    public String getResText() {
        return resText;
    }

    public void setResText(String resText) {
        this.resText = resText;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getLogTableMonth() {
        return logTableMonth;
    }

    public void setLogTableMonth(String logTableMonth) {
        this.logTableMonth = logTableMonth;
    }

    public String getDecisionTrace() {
        return decisionTrace;
    }

    public void setDecisionTrace(String decisionTrace) {
        this.decisionTrace = decisionTrace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DecisionLog{");
        sb.append("orgUuid='").append(orgUuid).append('\'');
        sb.append(", orgName='").append(orgName).append('\'');
        sb.append(", userUuid='").append(userUuid).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", microUuid='").append(microUuid).append('\'');
        sb.append(", deployUuid='").append(deployUuid).append('\'');
        sb.append(", pkgUuid='").append(pkgUuid).append('\'');
        sb.append(", pkgBaseline=").append(pkgBaseline);
        sb.append(", reqType=").append(reqType);
        sb.append(", state=").append(state);
        sb.append(", pass=").append(pass);
        sb.append(", effective=").append(effective);
        sb.append(", reqText='").append(reqText).append('\'');
        sb.append(", resText='").append(resText).append('\'');
        sb.append(", fullText='").append(fullText).append('\'');
        sb.append(", logTableMonth='").append(logTableMonth).append('\'');
        sb.append(", decisionTrace='").append(decisionTrace).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
