package com.beagledata.gaea.ruleengine.function;

import com.beagledata.gaea.ruleengine.annotation.FunctionMethodProperty;
import com.beagledata.gaea.ruleengine.annotation.FunctionProperty;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @Auther: yinrj
 * @Date: 0006 2019/6/6 14:19
 */
@FunctionProperty(name = "字符串函数")
public class StringFunction {
    @FunctionMethodProperty(name = "指定结束的字符串截取", params = {"字符串", "结束字符串"})
    public String subToEnd(String str, String subEndStr) {
        if (StringUtils.isBlank(str)) {
            return str;
        }

        if (StringUtils.isBlank(subEndStr)) {
            throw new RuleException("函数《指定结束的字符串截取》的参数《结束字符串》不能为空");
        }

        if (str.contains(subEndStr)) {
            return str.substring(0, str.indexOf(subEndStr));
        }
        return StringUtils.EMPTY;
    }

    @FunctionMethodProperty(name = "指定开始的字符串截取", params = {"字符串", "开始字符串"})
    public String subFromBegin(String str, String subBeginStr) {
        if (StringUtils.isBlank(str)) {
            return str;
        }

        if (StringUtils.isBlank(subBeginStr)) {
            throw new RuleException("函数《指定开始的字符串截取》的参数《开始字符串》不能为空");
        }

        if (str.contains(subBeginStr)) {
            return str.substring(str.indexOf(subBeginStr) + subBeginStr.length());
        }
        return StringUtils.EMPTY;
    }

    @FunctionMethodProperty(name = "指定起始的字符串截取", params = {"字符串", "开始字符串", "结束字符串"})
    public String subFromBeginToEnd(String str, String subBeginStr, String subEndStr) {

        if (StringUtils.isBlank(str)) {
            return str;
        }

        if (StringUtils.isBlank(subBeginStr)) {
            throw new RuleException("函数《指定起始的字符串截取》的参数《开始字符串》不能为空");
        }
        if (StringUtils.isBlank(subEndStr)) {
            throw new RuleException("函数《指定起始的字符串截取》的参数《结束字符串》不能为空");
        }
        int b = str.contains(subBeginStr) ? str.indexOf(subBeginStr) + subBeginStr.length() : -1;
        int e = str.contains(subEndStr) ? str.indexOf(subEndStr) : -1;
        if (b > e || b == -1 || e == -1) {
            return StringUtils.EMPTY;
        }
        return str.substring(b, e);

    }

    @FunctionMethodProperty(name = "字符首次出现的位置", params = {"字符串", "字符"})
    public Integer firstIndexOf(String str, String ch) {
        if (str != null) {
            return ch != null ? str.indexOf(ch) : -1;
        } else {
            return -1;
        }
    }

    @FunctionMethodProperty(name = "字符最后出现的位置", params = {"字符串", "字符"})
    public Integer lastIndexOf(String str, String ch) {
        if (str != null) {
            return ch != null ? str.lastIndexOf(ch) : -1;
        } else {
            return -1;
        }
    }

    @FunctionMethodProperty(name = "获取长度", params = {"字符串"})
    public Integer length(String str) {
        return StringUtils.isBlank(str) ? 0 : str.length();
    }

    @FunctionMethodProperty(name = "替换字符", params = {"字符串", "被替换字符串", "替换字符串"})
    public String replace(String str, String oldStr, String newStr) {
        return str != null ? str.replace(oldStr, newStr) : "";
    }

    @FunctionMethodProperty(name = "转小写", params = {"字符串"})
    public String toLowerCase(String str) {
        return str != null ? str.toLowerCase() : "";
    }

    @FunctionMethodProperty(name = "转大写", params = {"字符串"})
    public String toUpperCase(String str) {
        return str != null ? str.toUpperCase() : "";
    }

    @FunctionMethodProperty(name = "去空格", params = {"字符串"})
    public String trim(String str) {
        return str != null ? str.replaceAll(" ", "") : "";
    }

    @FunctionMethodProperty(name = "去除首位空格", params = {"字符串"})
    public String trimSpace(String str) {
        return StringUtils.trim(str);
    }


    @FunctionMethodProperty(name = "逗号分割字符串是否包含目标字符串", params = {"目标字符串", "逗号分隔字符串"})
    public Boolean contains(String targetStr, String delimitedStr) {
        String str[] = delimitedStr.split(",|，");//加上中文逗号，适配旧版本
        List<String> list = Arrays.asList(str);
        return list.contains(targetStr);
    }

    @FunctionMethodProperty(name = "指定开始索引的字符串截取", params = {"目标字符串", "开始索引"})
    public String subStringFromBegin(String str, Integer beginIndex) {
        return str.substring(beginIndex);
    }

    @FunctionMethodProperty(name = "指定结束索引的字符串截取", params = {"目标字符串", "结束索引"})
    public String subStringToEnd(String str, Integer endIndex) {
        return str.substring(0, endIndex);
    }

    @FunctionMethodProperty(name = "指定起始索引的字符串截取", params = {"目标字符串", "开始索引", "结束索引"})
    public String subStringFromBeginToEnd(String str, Integer beginIndex, Integer endIndex) {
        return str.substring(beginIndex, endIndex);
    }

    @FunctionMethodProperty(name = "数值转字符串", params = {"数值"})
    public String numberToString(Number number) {
        return String.valueOf(number);
    }

    @FunctionMethodProperty(name = "是否不为空", params = {"字符串"})
    public Boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }

    @FunctionMethodProperty(name = "是否为空", params = {"字符串"})
    public Boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    @FunctionMethodProperty(name = "拼接字符串", params = {"字符串1", "字符串2", "拼接符"})
    public String concat(String before, String after, String symbol) {
        if (null == symbol) {
            return String.format("%s%s", before, after);
        }
        return String.format("%s%s%s", before, symbol, after);
    }
}
