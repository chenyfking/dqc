package com.beagledata.gaea.workbench.rule.define;

/**
 * 算术类型
 *
 * Created by liulu on 2020/4/9.
 */
public enum ArithmeticType {
    ADD("+"), // 加法
    SUB("-"), // 减法
    MUL("*"), // 乘法
    DIV("/"); // 除法

    /**
     * 符号
     */
    private String symbol;

    ArithmeticType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
