package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * 回收站表
 **/

public class RecycleBin extends BaseEntity {

    private static final long serialVersionUID = 2843259034621252028L;
    /**
     * 文件uuid
     **/
    private String assetsUuid;
    /**
     * 文件名称
     */
    private String assetsName;
    /**
     * 文件类型
     **/
    private String assetsType;
    /**
     * 所属项目uuid
     */
    private String projectUuid;

    /**
     * 创建人
     */
    private User creator;
    /**
     * 类型，用于判断文件夹，模板
     */
    private String type;

    public RecycleBin() {
    }

    public RecycleBin(String uuid) {
        super.setUuid(uuid);
    }

    public String getAssetsUuid() {
        return assetsUuid;
    }

    public void setAssetsUuid(String assetsUuid) {
        this.assetsUuid = assetsUuid;
    }

    public String getAssetsType() {
        return assetsType;
    }

    public void setAssetsType(String assetsType) {
        this.assetsType = assetsType;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public String getAssetsName() {
        return assetsName;
    }

    public void setAssetsName(String assetsName) {
        this.assetsName = assetsName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RecycleBin{");
        sb.append("assetsUuid='").append(assetsUuid).append('\'');
        sb.append(", assetsName='").append(assetsName).append('\'');
        sb.append(", assetsType='").append(assetsType).append('\'');
        sb.append(", projectUuid='").append(projectUuid).append('\'');
        sb.append(", creator=").append(creator);
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
    