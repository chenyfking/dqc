package com.beagledata.gaea.workbench.rule.define;

import com.alibaba.fastjson.JSON;
import com.beagledata.gaea.workbench.entity.Assets;

import java.io.Serializable;
import java.util.List;

/**
 * Created by liulu on 2018/10/10.
 */
public class Constant implements Serializable {
    private static final long serialVersionUID = 4256628359949183125L;

    private int id;
    private String uuid;
    private String name;
    private List<Field> fields;

    public static Constant fromAssets(Assets assets) {
        Constant constant = JSON.parseObject(assets.getContent(), Constant.class);
        constant.setId(assets.getId());
        constant.setUuid(assets.getUuid());
        constant.setName(assets.getName());
        return constant;
    }

    public Field getFieldById(String id) {
        if (fields != null && !fields.isEmpty()) {
            for (Field field : fields) {
                if (field.getId().equals(id)) {
                    return field;
                }
            }
        }
        return null;
    }
    public Field getFieldByLabel(String label) {
        if (fields != null && !fields.isEmpty()) {
            for (Field field : fields) {
                if (label.equals(field.getLabel())) {
                    return field;
                }
            }
        }
        return null;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Constant{");
        sb.append("uuid='").append(uuid).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", fields=").append(fields);
        sb.append('}');
        return sb.toString();
    }
}
