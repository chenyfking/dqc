package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * 资源文件版本控制
 * Created by Chenyafeng on 2018/11/1.
 */
public class AssetsVersion extends BaseEntity {
    private static final long serialVersionUID = -680683996751984848L;
    //文件uuid
    private String assetUuid;
    private String assetsUuid;
    //文件内容
    private String content;
    //版本号
    private Integer versionNo;
    //版本描述
    private String versionDes;
    //创建人
    private User creator;
    //当前创建版本号
    private Integer nowVersion;
    //创建人名称
    private String creatorName;
    //文件名称
    private String assetsName;
    //文件描述
    private String description;
    //文件类型
    private String type;
    //所属项目uuid
    private String projectUuid;
    //项目名称
    private String projectName;
    //项目id
    private Integer projectId;

    public String getAssetsName() {
        return assetsName;
    }

    public void setAssetsName(String assetsName) {
        this.assetsName = assetsName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getAssetUuid() {
        return assetUuid;
    }

    public void setAssetUuid(String assetUuid) {
        this.assetUuid = assetUuid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getVersionDes() {
        return versionDes;
    }

    public void setVersionDes(String versionDes) {
        this.versionDes = versionDes;
    }

    public Integer getNowVersion() {
        return nowVersion;
    }

    public void setNowVersion(Integer nowVersion) {
        this.nowVersion = nowVersion;
    }

    public String getAssetsUuid() {
        return assetsUuid;
    }

    public void setAssetsUuid(String assetsUuid) {
        this.assetsUuid = assetsUuid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AssetsVersion{");
        sb.append("assetUuid='").append(assetUuid).append('\'');
        sb.append(", assetsUuid='").append(assetsUuid).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", versionNo=").append(versionNo);
        sb.append(", versionDes='").append(versionDes).append('\'');
        sb.append(", creator=").append(creator);
        sb.append(", nowVersion=").append(nowVersion);
        sb.append(", creatorName='").append(creatorName).append('\'');
        sb.append(", assetsName='").append(assetsName).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", projectUuid='").append(projectUuid).append('\'');
        sb.append(", projectName='").append(projectName).append('\'');
        sb.append(", projectId=").append(projectId);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
