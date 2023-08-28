package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * Created by Chenyafeng on 2018/12/20.
 */
public class MicroModel extends BaseEntity {
    private static final long serialVersionUID = -1561864838060511657L;
    /**
     * 服务的uuid
     */
    private String microUuid;

    /**
     * AI模型的uuid
     */
    private String modelUuid;

    public MicroModel() {}

    public MicroModel(String microUuid, String modelUuid) {
        this.microUuid = microUuid;
        this.modelUuid = modelUuid;
    }

    public String getMicroUuid() {
        return microUuid;
    }

    public void setMicroUuid(String microUuid) {
        this.microUuid = microUuid;
    }

    public String getModelUuid() {
        return modelUuid;
    }

    public void setModelUuid(String modelUuid) {
        this.modelUuid = modelUuid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MicroModel{");
        sb.append("microUuid='").append(microUuid).append('\'');
        sb.append(", modelUuid='").append(modelUuid).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
