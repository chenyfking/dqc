package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.ThirdApiDefinition;

import java.util.List;

/**
 * Created by liulu on 2020/6/14.
 */
public interface ThirdApiDefinitionService {
    /**
     * 添加自定义外部接口
     *
     * @param definition
     */
    void add(ThirdApiDefinition definition);

    /**
     * 获取所有外部接口
     *
     * @return
     */
    List<ThirdApiDefinition> list();

    /**
     * 建模查询所有外部接口
     *
     * @return
     */
    List<ThirdApiDefinition> listForModeling();

    /**
     * 删除外部接口
     *
     * @param uuid
     */
    Result delete(String uuid);

    /**
     * 编辑外部接口
     *
     * @param definition
     */
    void edit(ThirdApiDefinition definition);

    /**
     * @param uuid
     * @return
     */
    ThirdApiDefinition getByUuid(String uuid);
}
