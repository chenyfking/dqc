package com.beagledata.gaea.examples;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liulu on 2018/11/6.
 */
public class PercentageHandler {
    private static Map<String, PercentageHandler> instanceMap = new HashMap<>();

    private double rnd = Math.random();

    public static PercentageHandler getInstance(String id) {
        PercentageHandler percentageHandler = instanceMap.get(id);
        if (percentageHandler == null) {
            percentageHandler = new PercentageHandler();
            instanceMap.put(id, percentageHandler);
        }
        return percentageHandler;
    }

    public boolean evaluate(double percentage) {
        return (rnd -= percentage) < 0;
    }
}
