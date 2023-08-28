package com.beagledata.gaea.executioncore.service;

import com.beagledata.gaea.executioncore.entity.DecisionLog;

/**
 * Created by liulu on 2020/7/21.
 */
public interface DecisionLogService {
    /**
     * 添加决策日志
     *
     * @param log
     */
    void add(DecisionLog log);

    /**
    * 描述: 单条插入
    * @param: [log]
    * @author: 周庚新
    * @date: 2020/11/2
    * @return: void
    *
    */
    void insert(DecisionLog log);

    /**
    * 更新
    * @param: [log]
    * @author: 周庚新
    * @date: 2020/11/2
    * @return: void
    *
    */
    void update(DecisionLog log);

    /**
    * 描述: 初始化决策日志表
    * @param: []
    * @author: 周庚新
    * @date: 2020/11/2
    * @return: void
    *
    */
    void createTable();

    /**
    * 描述: 将写库失败的日志缓存到磁盘
    * @param: [log]
    * @author: 周庚新
    * @date: 2020/11/2
    * @return: void
    *
    */
    void  flushOnFailInsert(DecisionLog log);


    /**
    * 描述: 将更新失败的日志缓存到磁盘
    * @param: [log]
    * @author: 周庚新
    * @date: 2020/11/2
    * @return: void
    *
    */
    void  flushOnFailUpdate(DecisionLog log);


}
