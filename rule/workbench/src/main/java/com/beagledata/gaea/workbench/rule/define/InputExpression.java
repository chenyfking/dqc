package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.util.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 输入表达式
 *
 * Created by liulu on 2020/3/31.
 */
public class InputExpression implements Dumper, JavaTypeGetter {
    /**
     * 输入值
     */
    private String value;
    /**
     * 类型
     */
    private String type;

    public InputExpression() {
    }

    public InputExpression(Object value) {
        this.value = value.toString();
        this.type = value.getClass().getSimpleName();
    }

    @Override
    public String dump() {
        if (value == null) {
            return null;
        }

        return dumpByType(type);
    }

    @Override
    public String getJavaType() {
        return type;
    }

    private String dumpByType(String type) {
        if (StringUtils.isBlank(type)) {
            return String.format("%s", value.replace("\"", "\\\""));
        }
        if (String.class.getSimpleName().equals(type)) {
            return String.format("\"%s\"", value.replace("\"", "\\\""));
        } else if (BigDecimal.class.getSimpleName().equals(type) || (isNumber(type) && value.contains("."))) {
            return String.format("%sB", value);
        } else if (Date.class.getSimpleName().equals(type)) {
            return String.format(
                    "%s.parseDate(\"%s\", \"%s\")",
                    DateUtils.class.getName(), value, Constants.DEFAULT_DATE_FORMAT
            );
        } else if (type.startsWith(List.class.getSimpleName()) || type.startsWith(Set.class.getSimpleName())) {
            int index = type.indexOf('.');
            if (index != -1) {
                return dumpByType(type.substring(index + 1));
            }
        }
        return value;
    }

    private boolean isNumber(String type) {
        return "Integer".equals(type) || "Long".equals(type) || "Double".equals(type);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
