package com.beagledata.gaea.ruleengine.runtime;

/**
 * Created by liulu on 2020/5/9.
 */
public class RuleEngineFactory {
    public static RuleEngine getRuleEngine(String id) {
        return new RuleEngineImpl(id);
    }
}
