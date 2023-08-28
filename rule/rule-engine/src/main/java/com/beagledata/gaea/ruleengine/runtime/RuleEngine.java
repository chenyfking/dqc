package com.beagledata.gaea.ruleengine.runtime;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by liulu on 2020/5/8.
 */
public interface RuleEngine {
    /**
     * 设置数据模型字段的值
     *
     * @param id 数据模型标识
     * @param name 字段名称
     * @param value 字段值
     */
    void put(String id, String name, Object value);

    /**
     * 设置数据模型字段的值，自动根据字段名称选择模型对象
     *
     * @param name 字段名称
     * @param value 字段值
     */
    void put(String name, Object value);

    /**
     * 执行知识包
     */
    ExecutionResult execute() throws Exception;

    /**
     * 获取数据模型实例对象
     *
     * @param id 数据模型id
     * @return
     */
    Object getObject(String id);

    /**
     * @return 获取所有数据模型实例
     */
    List<Object> getObjects();

    /**
     * @return 获取所有数据模型实例
     */
    Map<String, Object> getObjectMap();

    /**
     * 获取某个数据模型String类型字段的值，找不到返回null
     *
     * @param id 数据模型标识
     * @param name 字段名称
     * @return 字段值
     */
    String getString(String id, String name);

    /**
     * 获取某个数据模型int类型字段的值，找不到返回0
     *
     * @param id 数据模型标识
     * @param name 字段名称
     * @return 字段值
     */
    int getInt(String id, String name);

    /**
     * 获取某个数据模型double类型字段的值，找不到返回0
     *
     * @param id 数据模型标识
     * @param name 字段名称
     * @return 字段值
     */
    double getDouble(String id, String name);

    /**
     * 获取某个数据模型long类型字段的值，找不到返回0
     *
     * @param id 数据模型标识
     * @param name 字段名称
     * @return 字段值
     */
    long getLong(String id, String name);

    /**
     * 获取某个数据模型BigDecimal类型字段的值，找不到返回new BigDecimal(0)
     *
     * @param id 数据模型标识
     * @param name 字段名称
     * @return 字段值
     */
    BigDecimal getBigDecimal(String id, String name);

    /**
     * 获取某个数据模型boolean类型字段的值，找不到返回false
     *
     * @param id 数据模型标识
     * @param name 字段名称
     * @return 字段值
     */
    boolean getBoolean(String id, String name);

    /**
     * 获取某个数据模型Date类型字段的值，找不到返回null
     *
     * @param id 数据模型标识
     * @param name 字段名称
     * @return 字段值
     */
    Date getDate(String id, String name);

    /**
     * 获取某个数据模型Object类型字段的值，找不到返回null
     *
     * @param id 数据模型标识
     * @param name 字段名称
     * @return 字段值
     */
    Object getObject(String id, String name);

    /**
     * @return 获取触发的规则
     */
    List<String> getFiredRules();

    /**
     * @return 获取执行结果实例
     */
    ExecutionResult getExecutionResult();
}
