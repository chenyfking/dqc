package com.beagledata.gaea.ruleengine.runtime;

import com.beagledata.gaea.ruleengine.builder.AbstractRuleBuilder;

import javax.sql.DataSource;

/**
 * Created by liulu on 2020/7/6.
 */
public class RuleContext {
    private static final ThreadLocal<RuleContext> threadLocal = new ThreadLocal<>();

    private AbstractRuleBuilder ruleBuilder;
    private static DataSource dataSource;

    public RuleContext(AbstractRuleBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
    }

    public static RuleContext getCurrentContext() {
        return threadLocal.get();
    }

    public static void setCurrentContext(RuleContext context) {
        removeCurrentContext();
        threadLocal.set(context);
    }

    public static void removeCurrentContext() {
        threadLocal.remove();
    }

    public AbstractRuleBuilder getRuleBuilder() {
        return ruleBuilder;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void setDataSource(DataSource dataSource) {
        RuleContext.dataSource = dataSource;
    }
}
