package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * 服务调用记录
 *
 * Created by liulu on 2020/5/19.
 */
public class ExecuteRecord extends BaseEntity {
    private static final long serialVersionUID = -7876093814263197030L;

    /**
     * 服务uuid
     */
    private String microUuid;
    /**
     * 请求参数
     */
    private String params;
    /**
     * 执行结果
     */
    private String result;

    public String getMicroUuid() {
        return microUuid;
    }

    public void setMicroUuid(String microUuid) {
        this.microUuid = microUuid;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ExecuteRecord{");
        sb.append("microUuid='").append(microUuid).append('\'');
        sb.append(", params='").append(params).append('\'');
        sb.append(", result='").append(result).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
