package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * 机构
 *
 * Created by liulu on 2018/1/5.
 */
public class Org extends BaseEntity {
    private static final long serialVersionUID = -2991725859105345314L;

    /**
     * 机构名称
     */
    private String name;
    /**
     * 机构编号
     */
    private String code;

    public Org() {
    }

    public Org(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Org{");
        sb.append("name='").append(name).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
