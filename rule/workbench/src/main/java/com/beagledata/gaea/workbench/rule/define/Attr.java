package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.gaea.workbench.common.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Created by liulu on 2018/9/30.
 */
public class Attr implements Dumper {
    public static final String RULEFLOW_GROUP = "ruleflow-group";
    public static final String LOCK_ON_ACTIVE = "lock-on-active";
    public static final String SALIENCE = "salience";
    public static final String ACTIVATION_GROUP = "activation-group";
    public static final String ENABLED = "enabled";
    public static final String NO_LOOP = "no-loop";

    private String name;
    private String value;

    public Attr() {
    }

    public Attr(String name) {
        this.name = name;
    }

    public Attr(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String dump() {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        if (ENABLED.equals(name) && "true".equals(value)) {
            return null;
        }

        if (ENABLED.equals(name) || SALIENCE.equals(name) || NO_LOOP.equals(name)) {
            return String.format("%s %s", name, value);
        }

        return String.format("%s \"%s\"", name, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        if ("date-effective".equals(name) || "date-expires".equals(name)) {
            try {
                return DateFormatUtils.format(
                        DateUtils.parseDate(value, "dd-MMM-yyyy"),
                        Constants.DEFAULT_DATE_FORMAT
                );
            } catch (Exception e) {
            }
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attr)) return false;

        Attr attr = (Attr) o;

        return getName().equals(attr.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Attr{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
