package com.beagledata.gaea.workbench.rule.define;

/**
 * 操作符
 *
 * Created by liulu on 2020/3/31.
 */
public enum Operator {
    GT("大于", ">"),
    GTE("大于或等于", ">="),
    LT("小于", "<"),
    LTE("小于或等于", "<="),
    EQ("等于", "=="),
    NEQ("不等于", "!="),
    MATCHES("匹配正则表达式", "matches"),
    NOT_MATCHES("不匹配正则表达式", "not matches"),
    CONTAINS("包含", "contains"),
    NOT_CONTAINS("不包含", "not contains"),
    STR_STARTSWITH("开始于", "str[startsWith]"),
    STR_NOT_STARTSWITH("不开始于", "not str[startsWith]"),
    STR_ENDSWITH("结束于", "str[endsWith]"),
    STR_NOT_ENDSWITH("不结束于", "not str[endsWith]"),
    STR_LENGTH("长度为", "str[length]"),
    STR_NOT_LENGTH("长度不为", "not str[length]");

    /**
     * 名称
     */
    private String name;
    /**
     * 符号
     */
    private String symbol;

    Operator(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
