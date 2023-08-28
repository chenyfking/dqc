package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.AssetsTemplate;

/**
 * 模板管理
 * Created by Chenyafeng on 2020/6/18
 */
public interface AssetsTemplateService {
    /**
     * 增加
     */
    Result add(String assetsUuid, String name);

    /**
     * 根据uuid查询
     */
    AssetsTemplate getByUuid(String uuid);

    /**
     * 根据类型、项目查询模板
     * Created by Chenyafeng on 2020/6/18
     */
    Result getTemplates(AssetsTemplate template);


    /**
     * 根据uuid更新模板内容
     */
    Result updateContentByUuid(String uuid, String content);

    /**
     * 删除模板
     */
    void delete(String uuid);
}
