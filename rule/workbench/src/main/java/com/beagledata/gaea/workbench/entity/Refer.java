package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * 引用实体类
 *
 * Created by liulu on 2020/11/11.
 */
public class Refer extends BaseEntity {
    private static final long serialVersionUID = 3555408493639312629L;

    /**
     * 被引用资源uuid
     */
    private String subjectUuid;
    /**
     * 被引用资源类型
     */
    private String subjectType;
    /**
     * 被引用资源子集
     */
    private String subjectChild;
    /**
     * 被引用资源版本
     */
    private Integer subjectVersion;
    /**
     * 引用资源uuid
     */
    private String referUuid;
    /**
     * 引用资源类型
     */
    private String referType;
    /**
     * 引用资源版本
     */
    private Integer referVersion;

    public Refer() {
    }

    public Refer(String subjectUuid, String subjectType, String subjectChild, Integer subjectVersion, String referUuid, String referType, Integer referVersion) {
        this.subjectUuid = subjectUuid;
        this.subjectType = subjectType;
        this.subjectChild = subjectChild;
        this.subjectVersion = subjectVersion;
        this.referUuid = referUuid;
        this.referType = referType;
        this.referVersion = referVersion;
    }

    public String getSubjectUuid() {
        return subjectUuid;
    }

    public void setSubjectUuid(String subjectUuid) {
        this.subjectUuid = subjectUuid;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectChild() {
        return subjectChild;
    }

    public void setSubjectChild(String subjectChild) {
        this.subjectChild = subjectChild;
    }

    public Integer getSubjectVersion() {
        return subjectVersion;
    }

    public void setSubjectVersion(Integer subjectVersion) {
        this.subjectVersion = subjectVersion;
    }

    public String getReferUuid() {
        return referUuid;
    }

    public void setReferUuid(String referUuid) {
        this.referUuid = referUuid;
    }

    public String getReferType() {
        return referType;
    }

    public void setReferType(String referType) {
        this.referType = referType;
    }

    public Integer getReferVersion() {
        return referVersion;
    }

    public void setReferVersion(Integer referVersion) {
        this.referVersion = referVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Refer refer = (Refer) o;

        if (subjectUuid != null ? !subjectUuid.equals(refer.subjectUuid) : refer.subjectUuid != null) return false;
        if (subjectType != null ? !subjectType.equals(refer.subjectType) : refer.subjectType != null) return false;
        if (subjectChild != null ? !subjectChild.equals(refer.subjectChild) : refer.subjectChild != null) return false;
        if (subjectVersion != null ? !subjectVersion.equals(refer.subjectVersion) : refer.subjectVersion != null)
            return false;
        if (referUuid != null ? !referUuid.equals(refer.referUuid) : refer.referUuid != null) return false;
        if (referType != null ? !referType.equals(refer.referType) : refer.referType != null) return false;
        return referVersion != null ? referVersion.equals(refer.referVersion) : refer.referVersion == null;
    }

    @Override
    public int hashCode() {
        int result = subjectUuid != null ? subjectUuid.hashCode() : 0;
        result = 31 * result + (subjectType != null ? subjectType.hashCode() : 0);
        result = 31 * result + (subjectChild != null ? subjectChild.hashCode() : 0);
        result = 31 * result + (subjectVersion != null ? subjectVersion.hashCode() : 0);
        result = 31 * result + (referUuid != null ? referUuid.hashCode() : 0);
        result = 31 * result + (referType != null ? referType.hashCode() : 0);
        result = 31 * result + (referVersion != null ? referVersion.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Refer{");
        sb.append("subjectUuid='").append(subjectUuid).append('\'');
        sb.append(", subjectType='").append(subjectType).append('\'');
        sb.append(", subjectChild='").append(subjectChild).append('\'');
        sb.append(", subjectVersion='").append(subjectVersion).append('\'');
        sb.append(", referUuid='").append(referUuid).append('\'');
        sb.append(", referType='").append(referType).append('\'');
        sb.append(", referVersion='").append(referVersion).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
