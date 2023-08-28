package com.beagledata.gaea.ruleengine.function;

import com.beagledata.gaea.ruleengine.annotation.FunctionMethodProperty;
import com.beagledata.gaea.ruleengine.annotation.FunctionProperty;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.util.BeanUtils;
import com.beagledata.gaea.ruleengine.util.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by liulu on 2019/5/10.
 */
@FunctionProperty(name = "List函数")
public class ListFunction {
    @FunctionMethodProperty(name = "求List大小", params = {"集合"})
    public Integer size(List list) {
        return list == null ? 0 : list.size();
    }

    @FunctionMethodProperty(name = "向List中添加对象", params = {"集合", "对象"})
    public void add(List list, Object obj) {
        if (list != null && obj != null) {
            list.add(BeanUtils.copy(obj));
        }
    }

    @FunctionMethodProperty(name = "向List中添加String", params = {"集合", "String"})
    public void add(List list, String obj) {
        if (list != null && obj != null) {
            list.add(obj);
        }
    }

    @FunctionMethodProperty(name = "向List中添加Integer", params = {"集合", "Integer"})
    public void add(List list, Integer obj) {
        if (list != null && obj != null) {
            list.add(obj);
        }
    }

    @FunctionMethodProperty(name = "向List中添加Double", params = {"集合", "Double"})
    public void add(List list, Double obj) {
        if (list != null && obj != null) {
            list.add(obj);
        }
    }

    @FunctionMethodProperty(name = "向List中添加Long", params = {"集合", "Long"})
    public void add(List list, Long obj) {
        if (list != null && obj != null) {
            list.add(obj);
        }
    }

    @FunctionMethodProperty(name = "向List中添加BigDecimal", params = {"集合", "BigDecimal"})
    public void add(List list, BigDecimal obj) {
        if (list != null && obj != null) {
            list.add(obj);
        }
    }

    @FunctionMethodProperty(name = "向List中添加Boolean", params = {"集合", "Boolean"})
    public void add(List list, Boolean obj) {
        if (list != null && obj != null) {
            list.add(obj);
        }
    }

    @FunctionMethodProperty(name = "向List中添加Date", params = {"集合", "Date"})
    public void add(List list, Date obj) {
        if (list != null && obj != null) {
            list.add(obj);
        }
    }

    // todo 是否加上 全部
    @FunctionMethodProperty(name = "从List中删除全部对象", params = {"集合", "对象"})
    public void remove(List list, Object obj) {
        removeObject(list, obj);
    }

    @FunctionMethodProperty(name = "从List中删除全部String", params = {"集合", "String"})
    public void remove(List list, String obj) {
        removeObject(list, obj);
    }

    @FunctionMethodProperty(name = "从List中删除全部Integer", params = {"集合", "Integer"})
    public void remove(List list, Integer obj) {
        removeObject(list, obj);
    }

    @FunctionMethodProperty(name = "从List中删除全部Double", params = {"集合", "Double"})
    public void remove(List list, Double obj) {
        removeObject(list, obj);
    }

    @FunctionMethodProperty(name = "从List中删除全部Long", params = {"集合", "Long"})
    public void remove(List list, Long obj) {
        removeObject(list, obj);
    }

    @FunctionMethodProperty(name = "从List中删除全部BigDecimal", params = {"集合", "BigDecimal"})
    public void remove(List list, BigDecimal obj) {
        removeObject(list, obj);
    }

    @FunctionMethodProperty(name = "从List中删除全部Boolean", params = {"集合", "Boolean"})
    public void remove(List list, Boolean obj) {
        removeObject(list, obj);
    }

    @FunctionMethodProperty(name = "从List中删除全部Date", params = {"集合", "Date"})
    public void remove(List list, Date obj) {
        removeObject(list, obj);
    }

    @FunctionMethodProperty(name = "对象是否在List中", params = {"集合", "对象"})
    public Boolean contains(List list, Object obj) {
        if (list != null && obj != null) {
            return list.contains(obj);
        }
        return false;
    }

    @FunctionMethodProperty(name = "String是否在List中", params = {"集合", "String"})
    public Boolean contains(List list, String obj) {
        if (list != null && obj != null) {
            return list.contains(obj);
        }
        return false;
    }

    @FunctionMethodProperty(name = "Integer是否在List中", params = {"集合", "Integer"})
    public Boolean contains(List list, Integer obj) {
        if (list != null && obj != null) {
            return list.contains(obj);
        }
        return false;
    }

    @FunctionMethodProperty(name = "Double是否在List中", params = {"集合", "Double"})
    public Boolean contains(List list, Double obj) {
        if (list != null && obj != null) {
            return list.contains(obj);
        }
        return false;
    }

    @FunctionMethodProperty(name = "Long是否在List中", params = {"集合", "Long"})
    public Boolean contains(List list, Long obj) {
        if (list != null && obj != null) {
            return list.contains(obj);
        }
        return false;
    }

    @FunctionMethodProperty(name = "BigDecimal是否在List中", params = {"集合", "BigDecimal"})
    public Boolean contains(List list, BigDecimal obj) {
        if (list != null && obj != null) {
            return list.contains(obj);
        }
        return false;
    }

    @FunctionMethodProperty(name = "Boolean是否在List中", params = {"集合", "Boolean"})
    public Boolean contains(List list, Boolean obj) {
        if (list != null && obj != null) {
            return list.contains(obj);
        }
        return false;
    }

    @FunctionMethodProperty(name = "Date是否在List中", params = {"集合", "Date"})
    public Boolean contains(List list, Date obj) {
        if (list != null && obj != null) {
            return list.contains(obj);
        }
        return false;
    }

    @FunctionMethodProperty(name = "计算List中数值之和", params = {"集合"})
    public Double sum(List list) {
        if (list == null || list.size() <= 0) {
            return 0.0;
        }

        BigDecimal sum = new BigDecimal("0");
        for (Object obj : list) {
            if (obj instanceof Number) {
                sum = sum.add(new BigDecimal(obj.toString()));
            }
        }
        return sum.doubleValue();
    }

    @FunctionMethodProperty(name = "计算List中对象字段数值之和", params = {"集合", "对象字段英文名称"})
    public Double sum(List list, String fieldKey) {
        if (list == null || list.size() <= 0) {
            return 0.0;
        }

        BigDecimal sum = new BigDecimal("0");
        try {
            for (Object obj : list) {
                if (obj == null) {
                    continue;
                }

                Object value = PropertyUtils.getProperty(obj, fieldKey);
                if (value instanceof Number) {
                    sum = sum.add(new BigDecimal(value.toString()));
                }
            }
        } catch (Exception e) {
            throw new RuleException(e);
        }
        return sum.doubleValue();
    }

    @FunctionMethodProperty(name = "获取list中的对象", params = {"集合", "序号"})
    public Object get(List list, Integer i) {
        if (list != null && i > 0 && i <= list.size()) {
            return list.get(i - 1);
        }
        return null;
    }

    @FunctionMethodProperty(name = "获取list中的String", params = {"集合", "序号"})
    public String getString(List list, Integer i) {
        if (list != null && i > 0 && i <= list.size()) {
            Object obj = list.get(i - 1);
            if (obj instanceof String) {
                return (String) obj;
            }
        }
        return StringUtils.EMPTY;
    }

    @FunctionMethodProperty(name = "获取list中的Integer", params = {"集合", "序号"})
    public Integer getInteger(List list, Integer i) {
        if (list != null && i > 0 && i <= list.size()) {
            Object obj = list.get(i - 1);
            if (obj instanceof Integer) {
                return (Integer) obj;
            }
        }
        return 0;
    }

    @FunctionMethodProperty(name = "获取list中的Double", params = {"集合", "序号"})
    public BigDecimal getDouble(List list, Integer i) {
        if (list != null && i > 0 && i <= list.size()) {
            Object obj = list.get(i - 1);
            if (obj instanceof BigDecimal) {
                return (BigDecimal) obj;
            } else {
                return new BigDecimal(String.valueOf(obj));
            }
        }
        return new BigDecimal(0);
    }

    @FunctionMethodProperty(name = "获取list中的Long", params = {"集合", "序号"})
    public Long getLong(List list, Integer i) {
        if (list != null && i > 0 && i <= list.size()) {
            Object obj = list.get(i - 1);
            if (obj instanceof Long) {
                return (Long) obj;
            }
        }
        return 0L;
    }

    @FunctionMethodProperty(name = "获取list中的BigDecimal", params = {"集合", "序号"})
    public BigDecimal getBigDecimal(List list, Integer i) {
        if (list != null && i > 0 && i <= list.size()) {
            Object obj = list.get(i - 1);
            if (obj instanceof BigDecimal) {
                return (BigDecimal) obj;
            }
        }
        return new BigDecimal(0);
    }

    @FunctionMethodProperty(name = "获取list中的Boolean", params = {"集合", "序号"})
    public Boolean getBoolean(List list, Integer i) {
        if (list != null && i > 0 && i <= list.size()) {
            Object obj = list.get(i - 1);
            if (obj instanceof Boolean) {
                return (Boolean) obj;
            }
        }
        return false;
    }

    @FunctionMethodProperty(name = "获取list中的Date", params = {"集合", "序号"})
    public Date getDate(List list, Integer i) {
        if (list != null && i > 0 && i <= list.size()) {
            Object obj = list.get(i - 1);
            if (obj instanceof Date) {
                return (Date) obj;
            }
        }
        return null;
    }

    private void removeObject(List list, Object obj) {
        if (list != null && obj != null) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                if (obj.equals(it.next())) {
                    it.remove();
                }
            }
        }
    }

}
