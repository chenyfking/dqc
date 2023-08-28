package com.beagledata.gaea.workbench.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.beagledata.gaea.common.BaseEntity;
import com.beagledata.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mahongfei on 2018/10/8.
 */
public class KnowledgePackage extends BaseEntity {
    private static final long serialVersionUID = 7245960684289081112L;

    /**
     * 编码，SDK调用使用
     */
    private String code;
    /**
     * 知识包名称
     */
    private String name;
    /**
     * 知识包描述
     */
    private String description;
    /**
     * 所属项目uuid
     */
    private String projectUuid;
    /**
     * 知识包uuid
     */
    private String packageUuid;
    /**
     * 资源文件uuid
     */
    private String assetsUuid;
    /**
     * 资源文件列表
     */
    private List<Assets> assetsList = new ArrayList<>();
    /**
     * 服务uuid
     */
    private String microUuid;
    /**
     * 创建用户
     */
    private User creator;
    /**
     * 资源文件版本号
     */
    private Integer assetsVersion;
    /**
     * 基线版本号
     */
    private Integer baselineVersion;
    /**
     * 知识包基线
     */
    private List<KnowledgePackageBaseline> baselines;
    /**
     * 待审核基线树
     */
    private Integer auditCount;

    public KnowledgePackage() {
    }

    public KnowledgePackage(String uuid) {
        super.setUuid(uuid);
    }

    /**
     * @return 获取内容不为空的资源文件
     */
    @JSONField(serialize = false)
    @JsonIgnore
    public List<Assets> getAvailableAssetsList() {
        return assetsList.stream().filter(assets -> StringUtils.isNotBlank(assets.getContent())).collect(Collectors.toList());
    }

    public String getCode() {
        if (StringUtils.isBlank(code)) {
            return String.valueOf(getId());
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public String getPackageUuid() {
        return packageUuid;
    }

    public void setPackageUuid(String packageUuid) {
        this.packageUuid = packageUuid;
    }

    public String getAssetsUuid() {
        return assetsUuid;
    }

    public void setAssetsUuid(String assetsUuid) {
        this.assetsUuid = assetsUuid;
    }

    public List<Assets> getAssetsList() {
        return assetsList;
    }

    public void setAssetsList(List<Assets> assetsList) {
        this.assetsList = assetsList;
    }

    public String getMicroUuid() {
        return microUuid;
    }

    public void setMicroUuid(String microUuid) {
        this.microUuid = microUuid;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Integer getAssetsVersion() {
        return assetsVersion;
    }

    public void setAssetsVersion(Integer assetsVersion) {
        this.assetsVersion = assetsVersion;
    }

    public Integer getBaselineVersion() {
        return baselineVersion;
    }

    public void setBaselineVersion(Integer baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    public List<KnowledgePackageBaseline> getBaselines() {
        return baselines;
    }

    public void setBaselines(List<KnowledgePackageBaseline> baselines) {
        this.baselines = baselines;
    }

    public Integer getAuditCount() {
        return auditCount;
    }

    public void setAuditCount(Integer auditCount) {
        this.auditCount = auditCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("KnowledgePackage{");
        sb.append("code='").append(code).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", projectUuid='").append(projectUuid).append('\'');
        sb.append(", packageUuid='").append(packageUuid).append('\'');
        sb.append(", assetsUuid='").append(assetsUuid).append('\'');
        sb.append(", assetsList=").append(assetsList);
        sb.append(", microUuid='").append(microUuid).append('\'');
        sb.append(", creator=").append(creator);
        sb.append(", assetsVersion=").append(assetsVersion);
        sb.append(", baselineVersion=").append(baselineVersion);
        sb.append(", baselines=").append(baselines);
        sb.append(", auditCount=").append(auditCount);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}