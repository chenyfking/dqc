package com.beagledata.gaea.workbench.rule.define;

/**
 * 连接符
 *
 * Created by liulu on 2020/3/31.
 */
public enum Conjunction {
    AND("&&", "并且"),
    OR("||", "或者"),
    NOT("!", "非");

    /**
     * 符号
     */
    private String symbol;
    /**
     * 名称
     */
    private String name;

    Conjunction(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
