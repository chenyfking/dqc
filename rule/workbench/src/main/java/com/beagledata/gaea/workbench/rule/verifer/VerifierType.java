package com.beagledata.gaea.workbench.rule.verifer;

/**
 * Created by liulu on 2020/11/11.
 */
public enum VerifierType {
    /**
     * 范围缺失
     */
    MISSING_RANGE,
    /**
     * 重复规则
     */
    EQUIVALENT,
    /**
     * 重叠条件
     */
    OVERLAP,
    /**
     * 规则不一致
     */
    INCOHERENT,
    /**
     * 永真规则
     */
    ALWAYS_TRUE
}
