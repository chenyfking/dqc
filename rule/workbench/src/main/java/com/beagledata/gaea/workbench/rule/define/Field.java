package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.gaea.ruleengine.annotation.PassingDirection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Created by liulu on 2018/9/30.
 */
public class Field implements Serializable {
    private static final long serialVersionUID = -7288950605531113855L;

    private String id;
    private String name;
    private String label;
    private Type type;
    private String subType;
    private String value;
    private String original;//仿真测试参数
    private String deriveData;//衍生变量 数据
    private String expectedValue;//期望值
    /**
     * 结果输出，默认输入输出
     */
    private PassingDirection.Direction direction = PassingDirection.Direction.IN_OUT;
    /**
     * 是否必输
     */
    private String required;

    public enum Type {
        String, Integer, Double, Long, BigDecimal, Boolean, Date, List, Set, Map, Object, Derive
    }

    /**
     * @return 是否必输
     */
    @JsonIgnore
    public boolean isRequired() {
        return "true".equals(required);
    }

    /**
     * @return 是否衍生变量
     */
    @JsonIgnore
    public boolean isDeriveType() {
        return Type.Derive.equals(type);
    }

    /**
     * Map和Object统一当做对象处理
     *
     * @return 是否对象类型
     */
    @JsonIgnore
    public boolean isObjectType() {
        return Type.Map.equals(type) || Type.Object.equals(type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public PassingDirection.Direction getDirection() {
        return direction;
    }

    public void setDirection(PassingDirection.Direction direction) {
        this.direction = direction;
    }

    public String getDeriveData() {
        return deriveData;
    }

    public void setDeriveData(String deriveData) {
        this.deriveData = deriveData;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Field{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", label='").append(label).append('\'');
        sb.append(", type=").append(type);
        sb.append(", subType='").append(subType).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", original='").append(original).append('\'');
        sb.append(", deriveData='").append(deriveData).append('\'');
        sb.append(", expectedValue='").append(expectedValue).append('\'');
        sb.append(", direction=").append(direction);
        sb.append(", required='").append(required).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
