package com.beagledata.gaea.workbench.rule.define;

import com.alibaba.fastjson.JSON;
import com.beagledata.gaea.ruleengine.annotation.PassingDirection;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liulu on 2018/9/30.
 */
public class Fact implements Import, Serializable {
    private static final long serialVersionUID = 813190932989209815L;

    public static final String DEFAULT_PACKAGE = "com.beagledata.gaea.fact";

    private int id;
    private String uuid;
    @JsonIgnore
    private String packageName = DEFAULT_PACKAGE;
    private String name;
    private List<Field> fields = new ArrayList<>();
    private String className;
    /**
     * 英文名称
     */
    private String enName;
    /**
     * 传递方向
     */
    private PassingDirection.Direction direction = PassingDirection.Direction.IN_OUT;

    public static Fact fromAssets(Assets assets) {
        Fact fact = JSON.parseObject(assets.getContent(), Fact.class);
        fact.setId(assets.getId());
        fact.setClassName("Fact_" + fact.getId());
        fact.setUuid(assets.getUuid());
        fact.setName(assets.getName());
        return fact;
    }

    public Fact() {
    }

    public Fact(int id) {
        this.id = id;
    }

    public Fact(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }

    @Override
    public String getImport() {
        return String.format("%s.%s", packageName, className);
    }

    public String getIdentity() {
        return StringUtils.isNotBlank(enName) ? enName : String.valueOf(id);
    }

    public Field getFieldById(String id) {
        return fields.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);
    }

    public Field getFieldByName(String name) {
        return fields.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
    }
    public Field getFieldByLabelAndName(String label, String name) {
       return fields.stream().filter(f -> f.getName().equals(name)&& f.getLabel().equals(label)).findFirst().orElse(null);
    }
    //将用户输入的值original赋值到value
    public void setFieldValueFromOriginal() {
        for (Field field : fields) {
            field.setValue(field.getOriginal());
        }
    }

    //清空original的值
    public void clearOriginal() {
        if (fields != null && !fields.isEmpty()) {
            for (Field field : fields) {
                field.setOriginal(null);
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public PassingDirection.Direction getDirection() {
        return direction;
    }

    public void setDirection(PassingDirection.Direction direction) {
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fact)) return false;

        Fact fact = (Fact) o;

        if (getId() != fact.getId()) return false;
        if (getPackageName() != null ? !getPackageName().equals(fact.getPackageName()) : fact.getPackageName() != null)
            return false;
        return getClassName() != null ? getClassName().equals(fact.getClassName()) : fact.getClassName() == null;
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + (getPackageName() != null ? getPackageName().hashCode() : 0);
        result = 31 * result + (getClassName() != null ? getClassName().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Fact{");
        sb.append("id=").append(id);
        sb.append(", uuid='").append(uuid).append('\'');
        sb.append(", packageName='").append(packageName).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", fields=").append(fields);
        sb.append(", className='").append(className).append('\'');
        sb.append(", enName='").append(enName).append('\'');
        sb.append(", direction=").append(direction);
        sb.append('}');
        return sb.toString();
    }


}
