package com.beagledata.gaea.ruleengine.runtime.flow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liulu on 2018/11/6.
 */
public class PercentageDecisionHandler {
    private Map<String, PercentageDecisionHandler> instanceMap = new HashMap<>();

    private double rnd = Math.random();

    public PercentageDecisionHandler getInstance(String id) {
        PercentageDecisionHandler percentageHandler = instanceMap.get(id);
        if (percentageHandler == null) {
            percentageHandler = new PercentageDecisionHandler();
            instanceMap.put(id, percentageHandler);
        }
        return percentageHandler;
    }

    public boolean evaluate(Double percentage) {
        return percentage != null ? (rnd -= percentage) < 0 : true;
    }
}
