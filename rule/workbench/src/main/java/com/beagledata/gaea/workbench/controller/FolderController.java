package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.Folder;
import com.beagledata.gaea.workbench.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by mahongfei on 2018/11/27.
 */
@RestController
@RequestMapping("folder")
public class FolderController {
    @Autowired
    private FolderService folderService;

    /**
     * @Author: mahongfei
     * @description: 添加文件夹
     */
    @PostMapping("")
    @RestLogAnnotation(describe = "添加文件夹")
    public Result addFolder(Folder folder) {
        if (folder.getUuid() == null) {
            String uuid = folderService.addFolder(folder);
            return Result.newSuccess().withData(uuid);
        } else {
            folderService.editFolder(folder);
            return Result.newSuccess();
        }
    }

    /**
     * @Author: mahongfei
     * @description: 获取文件夹列表
     */
    @GetMapping("")
    public Result<List<Object>> listAll(String projectUuid){
        return Result.newSuccess().withData(folderService.listAll(projectUuid));
    }

    /**
     *@Author: mahongfei
     *@description: 删除文件夹
     */
    @PostMapping("delete")
    @RestLogAnnotation(describe = "删除文件夹")
    public Result deleteFolder(String uuid) {
        return folderService.deleteFolder(uuid);
    }

    @GetMapping("lock")
    @RestLogAnnotation(describe = "锁定文件夹")
    public Result lock(String uuid) {
        return folderService.lockFolder(uuid, true);
    }

    @GetMapping("unlock")
    @RestLogAnnotation(describe = "解锁文件夹")
    public Result unlock(String uuid) {
        return folderService.lockFolder(uuid, false);
    }
}
