package com.beagledata.gaea.ruleengine.model;

import java.io.Serializable;

/**
 * Created by liulu on 2020/5/11.
 */
public class RuleFactModel implements Serializable {
    private static final long serialVersionUID = -3402870865533137702L;

    /**
     * 标识当前Fact对象
     */
    private String id;
    /**
     * 当前Fact对象的类路径
     */
    private String className;

    public RuleFactModel() {
    }

    public RuleFactModel(String id) {
        this.id = id;
    }

    public RuleFactModel(String id, String className) {
        this.id = id;
        this.className = className;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RuleFactModel{");
        sb.append("id='").append(id).append('\'');
        sb.append(", className='").append(className).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
