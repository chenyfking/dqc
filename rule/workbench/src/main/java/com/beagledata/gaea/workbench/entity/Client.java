package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * 规则服务器（集群节点）
 *
 * @author liulu
 * 2020/5/13 11:43
 */
public class Client extends BaseEntity {
    private static final long serialVersionUID = 1607030448566103059L;

    /**
     * 名称
     */
    private String name;
    /**
     * 连接地址
     */
    private String baseUrl;
    /**
     * 是否禁用
     */
    private Boolean disabled;
    /**
     * 序列号
     */
    private String serialNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Client{");
        sb.append("name='").append(name).append('\'');
        sb.append(", baseUrl='").append(baseUrl).append('\'');
        sb.append(", disabled=").append(disabled);
        sb.append(", serialNumber='").append(serialNumber).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
