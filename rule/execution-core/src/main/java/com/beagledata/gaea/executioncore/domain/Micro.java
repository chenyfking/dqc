package com.beagledata.gaea.executioncore.domain;

import com.beagledata.gaea.common.RestConstants;

import java.util.Date;

/**
 * Created by liulu on 2020/7/14.
 */
public class Micro {
    /**
     * 服务id
     */
    private String id;
    /**
     * 发布时间
     */
    private Date deployTime;
    /**
     * 服务类型
     */
    private RestConstants.MicroType microType;
    /**
     * 上线配置
     */
    private MicroDeployment deployment;
    /**
     * 所有者
     */
    private MicroOwner owner;
    /**
     * 审批字段
     */
    private ApprovalLabel approvalLabel;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(Date deployTime) {
        this.deployTime = deployTime;
    }

    public RestConstants.MicroType getMicroType() {
        return microType;
    }

    public void setMicroType(RestConstants.MicroType microType) {
        this.microType = microType;
    }

    public MicroDeployment getDeployment() {
        return deployment;
    }

    public void setDeployment(MicroDeployment deployment) {
        this.deployment = deployment;
    }

    public MicroOwner getOwner() {
        return owner;
    }

    public void setOwner(MicroOwner owner) {
        this.owner = owner;
    }

    public ApprovalLabel getApprovalLabel() {
        return approvalLabel;
    }

    public void setApprovalLabel(ApprovalLabel approvalLabel) {
        this.approvalLabel = approvalLabel;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Micro{");
        sb.append("id='").append(id).append('\'');
        sb.append(", deployTime=").append(deployTime);
        sb.append(", microType=").append(microType);
        sb.append(", deployment=").append(deployment);
        sb.append(", owner=").append(owner);
        sb.append(", approvalLabel=").append(approvalLabel);
        sb.append('}');
        return sb.toString();
    }
}
