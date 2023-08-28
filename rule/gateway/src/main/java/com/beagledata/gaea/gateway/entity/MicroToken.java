package com.beagledata.gaea.gateway.entity;

import com.beagledata.gaea.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * 服务token
 *
 * Created by liulu on 2020/6/29.
 */
public class MicroToken extends BaseEntity {
    private static final long serialVersionUID = -4115184601484032207L;

    /**
     * token
     */
    private String token;
    /**
     * 所有服务
     */
    private boolean all;
    /**
     * 服务uuid列表
     */
    private Set<String> microUuids = new HashSet<>();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public Set<String> getMicroUuids() {
        return microUuids;
    }

    public void setMicroUuids(Set<String> microUuids) {
        this.microUuids = microUuids;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MicroToken)) return false;

        MicroToken token1 = (MicroToken) o;

        return getToken() != null ? getToken().equals(token1.getToken()) : token1.getToken() == null;
    }

    @Override
    public int hashCode() {
        return getToken() != null ? getToken().hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MicroToken{");
        sb.append("token='").append(token).append('\'');
        sb.append(", all=").append(all);
        sb.append(", microUuids=").append(microUuids);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
