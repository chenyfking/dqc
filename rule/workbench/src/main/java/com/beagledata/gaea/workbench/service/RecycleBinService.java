package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.RecycleBin;

/**
 * 回收站管理
 * Created by Chenyafeng on 2020/6/24
 */
public interface RecycleBinService {
    /**
     * 查询回收站列表
     */
    Result getListByParam(int page, int pageNum, RecycleBin recycleBin);

    /**
     * 批量还原
     * projectUuid 项目uuid
     * uuids: 逗号分隔的文件uuid
     */
    Result batchRestore(String projectUuid, String uuids);

    /**
     * 批量删除
     * @param deleteJson
     */
    void batchDelete(String deleteJson);

    /**
     * 清空项目的回收站
     */
    void setEmpty(String projectUuid);

}
