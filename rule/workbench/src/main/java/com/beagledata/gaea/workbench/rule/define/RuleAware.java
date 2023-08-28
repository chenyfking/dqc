package com.beagledata.gaea.workbench.rule.define;

/**
 * 设置规则对象
 *
 * Created by liulu on 2020/4/10.
 */
public interface RuleAware {
    void setRule(Rule rule);

    static void setRuleUnchecked(Rule rule, RuleAware... awares) {
        if (awares != null && awares.length > 0) {
            for (RuleAware aware : awares) {
                if (aware != null) {
                    aware.setRule(rule);
                }
            }
        }
    }
}
