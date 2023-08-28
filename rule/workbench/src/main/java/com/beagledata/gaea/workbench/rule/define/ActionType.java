package com.beagledata.gaea.workbench.rule.define;

/**
 * 动作类型
 *
 * Created by liulu on 2020/4/9.
 */
public enum ActionType {
    /**
     * 赋值
     */
    ASSIGN,
    /**
     * 执行函数
     */
    FUNC,
    /**
     * 调用外部接口
     */
    THIRDAPI,
    /**
     * 调用机器学习模型
     */
    MODEL
}
