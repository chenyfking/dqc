package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * Created by Chenyafeng on 2018/12/5.
 */
public class MicroType extends BaseEntity {

    private static final long serialVersionUID = -4796495385073796779L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MicroType() {}

    public MicroType (String uuid) {
        super.setUuid(uuid);
    }

    public MicroType (String uuid, String name) {
        super.setUuid(uuid);
        this.name = name;
    }

    @Override
    public String toString() {
        return "MicroType{" +
                ", name='" + name + '\'' +
                "} " + super.toString();
    }
}
