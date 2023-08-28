package com.beagledata.gaea.executioncore.handler;

import com.beagledata.gaea.ruleengine.builder.AbstractRuleBuilder;
import com.beagledata.gaea.ruleengine.builder.ByteArrayRuleBuilder;
import com.beagledata.gaea.ruleengine.runtime.RuleContainer;

/**
 * Created by liulu on 2020/7/20.
 */
public class ByteArrayRuleContainer extends RuleContainer {
    private ByteArrayRuleContainer() {}

    static {
        INSTANCE.compareAndSet(null, new ByteArrayRuleContainer());
    }

    public static String loadRule(byte[] bytes) {
        AbstractRuleBuilder builder = ByteArrayRuleBuilder.newBuilder(bytes);
        return loadRule(builder);
    }
}
