package com.beagledata.gaea.ruleengine.aimodel;

/**
 * Created by liulu on 2020/7/6.
 */
public interface Invoker {
    String invoke(Object input) throws Exception;
}
