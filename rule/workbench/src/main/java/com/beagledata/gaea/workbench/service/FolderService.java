package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.Folder;

import java.util.List;

/**
 * Created by mahongfei on 2018/11/27.
 */
public interface FolderService {
    /**
     *@Author: mahongfei
     *@description: 添加文件夹
     */
    String addFolder(Folder folder);

    /**
     *@Author: mahongfei
     *@description: 文件夹列表
     */
    List<Object> listAll(String projectUuid);

    /**
     *@Author: mahongfei
     *@description: 修改文件夹
     */
    void editFolder(Folder folder);

    /**
     *@Author: mahongfei
     *@description: 删除文件夹
     */
    Result deleteFolder(String uuid);

    /**
     *@Author: mahongfei
     *@description: 锁定/解锁文件夹
     */
    Result lockFolder(String uuid, Boolean isLock);
}
