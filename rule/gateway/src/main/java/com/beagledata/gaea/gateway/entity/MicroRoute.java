package com.beagledata.gaea.gateway.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * Created by liulu on 2020/6/29.
 */
public class MicroRoute extends BaseEntity {
    private static final long serialVersionUID = -8624221749817235667L;

    /**
     * 服务uuid
     */
    private String microUuid;
    /**
     * 服务地址
     */
    private String url;

    public String getMicroUuid() {
        return microUuid;
    }

    public void setMicroUuid(String microUuid) {
        this.microUuid = microUuid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MicroRoute{");
        sb.append("microUuid='").append(microUuid).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
