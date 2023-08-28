package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * @Auther: yinrj
 * @Date: 0028 2020/6/28 15:34
 * @Description: 知识包基线
 */
public class KnowledgePackageBaseline extends BaseEntity {
    private static final long serialVersionUID = 3210880849353594149L;
    /**
     * 知识包uuid
     */
    private String packageUuid;
    /**
     * 版本号
     */
    private Integer versionNo;
    /**
     * 版本描述
     */
    private String versionDesc;
    /**
     * 创建人
     */
    private User creator;
    /**
     * 状态： 0 待发布 1 待审核 2 待生效 3 已生效 4 已拒绝
     */
    private Integer state;
    /**
     * 审核理由
     */
    private String auditReason;

    public String getPackageUuid() {
        return packageUuid;
    }

    public void setPackageUuid(String packageUuid) {
        this.packageUuid = packageUuid;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getAuditReason() {
        return auditReason;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("KnowledgePackageBaseline{");
        sb.append("packageUuid='").append(packageUuid).append('\'');
        sb.append(", versionNo=").append(versionNo);
        sb.append(", versionDesc='").append(versionDesc).append('\'');
        sb.append(", creator=").append(creator);
        sb.append(", state=").append(state);
        sb.append(", auditReason='").append(auditReason).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
