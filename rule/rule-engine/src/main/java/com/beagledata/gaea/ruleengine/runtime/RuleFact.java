package com.beagledata.gaea.ruleengine.runtime;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liulu on 2019/1/4.
 */
public class RuleFact implements Serializable {
    private static final long serialVersionUID = -6865468930340938482L;

    /**
     * 标识当前Fact对象
     */
    private String id;
    /**
     * 当前Fact对象的类路径
     */
    private String className;
    /**
     * 对象的字段集合说明
     */
    @JSONField(serialize = false)
    private Map<String, Class> fields = new HashMap<>();
    /**
     * insert到KieSession中的实例对象
     */
    @JSONField(serialize = false)
    private Object obj;

    public RuleFact(String id, String className) {
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

    public Map<String, Class> getFields() {
        return fields;
    }

    public void setFields(Map<String, Class> fields) {
        this.fields = fields;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            fields.put(field.getName(), field.getType());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleFact)) return false;

        RuleFact ruleFact = (RuleFact) o;

        return getId() != null ? getId().equals(ruleFact.getId()) : ruleFact.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RuleFact{");
        sb.append("id=").append(id);
        sb.append(", fields=").append(fields);
        sb.append(", obj=").append(obj);
        sb.append('}');
        return sb.toString();
    }
}
