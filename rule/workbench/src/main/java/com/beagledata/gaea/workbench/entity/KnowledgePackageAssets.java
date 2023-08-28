package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * Created by mahongfei on 2018/11/6.
 */
public class KnowledgePackageAssets extends BaseEntity {
    private static final long serialVersionUID = 7267786195654090857L;
    private Integer id;
    private String packageUuid;
    private String assetsUuid;
    private Integer assetsVersion;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
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

    public Integer getAssetsVersion() {
        return assetsVersion;
    }

    public void setAssetsVersion(Integer assetsVersion) {
        this.assetsVersion = assetsVersion;
    }

    @Override
    public String toString() {
        return "KnowledgePackageAssets{" +
                "id=" + id +
                ", packageUuid='" + packageUuid + '\'' +
                ", assetsUuid='" + assetsUuid + '\'' +
                ", assetsVersion=" + assetsVersion +
                '}';
    }
}
