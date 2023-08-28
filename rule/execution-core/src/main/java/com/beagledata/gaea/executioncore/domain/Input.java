package com.beagledata.gaea.executioncore.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口输入
 *
 * Created by liulu on 2020/5/11.
 */
public class Input implements Serializable {
    private static final long serialVersionUID = -8875809953456085095L;

    /**
     * 请求唯一标识
     */
    @JsonIgnore
    private String uuid;

    /**
     * 异步请求标识
     */
    @JsonIgnore
    private boolean async;
    /**
     * 请求时间
     */
    @JsonIgnore
    private Date reqDate;

    /**
     * 服务标识
     */
    @JsonIgnore
    private String service;
    /**
     * 测试集数据
     */
    private Map<String, Object> data = new HashMap<>();

    public Input() {
    }

    public Input(String service) {
        this.service = service;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public Date getReqDate() {
        return reqDate;
    }

    public void setReqDate(Date reqDate) {
        this.reqDate = reqDate;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Input{");
        sb.append("service='").append(service).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
