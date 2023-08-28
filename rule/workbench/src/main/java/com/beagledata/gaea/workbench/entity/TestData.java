package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * 仿真测试数据
 *
 * Created by liulu on 2018/1/25.
 */
public class TestData extends BaseEntity {
    /**
     * 知识包id
     */
    private String packageId;
    /**
     * 测试数据
     */
    private String data;

    /**
    * 知识包基线版本号
    */
    private Integer baselineVersion;

    public TestData() {
    }

    public TestData(String packageId, String data) {
        this.packageId = packageId;
        this.data = data;
    }
    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getBaselineVersion() {
        return baselineVersion;
    }

    public void setBaselineVersion(Integer baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TestData{");
        sb.append(", packageId='").append(packageId).append('\'');
        sb.append(", baselineVersion='").append(baselineVersion).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
