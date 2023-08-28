package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.rule.util.AssetsCache;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 常量表达式
 *
 * Created by liulu on 2020/4/8.
 */
public class ConstantExpression implements Dumper {
    /**
     * 常量uuid
     */
    private String id;
    /**
     * 常量字段id
     */
    private String fieldId;

    @Override
    public String dump() {
        Constant constant = AssetsCache.getConstantByUuid(id);
        if (constant == null) {
            return null;
        }
        Field field = constant.getFieldById(fieldId);
        if (field == null) {
            return null;
        }

        String type = field.getType().name();

        if (String.class.getSimpleName().equals(type)) {
            return String.format("\"%s\"", field.getValue().replace("\"", "\\\""));
        }
        if (BigDecimal.class.getSimpleName().equals(type) || (isNumber(type) && field.getValue().contains("."))) {
            return String.format("%sB", field.getValue());
        }
        if (Date.class.getSimpleName().equals(type)) {
            return String.format(
                    "%s.parseDate(\"%s\", \"%s\")",
                    DateUtils.class.getName(), field.getValue(), Constants.DEFAULT_DATE_FORMAT
            );
        }
        return field.getValue();
    }

    private boolean isNumber(String type) {
        return "Integer".equals(type) || "Long".equals(type) || "Double".equals(type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }
}
