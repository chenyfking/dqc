package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.entity.RecycleBin;
import com.beagledata.gaea.workbench.service.RecycleBinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 回收站管理
 * Created by Chenyafeng on 2020/6/24
 */
@RestController
@RequestMapping("recycle")
public class RecycleBinController {

    @Autowired
    private RecycleBinService recycleBinService;

    /**
     * 查看项目的回收站
     */
    @GetMapping("")
    public Result recycleByProject(@RequestParam(required = false, defaultValue = "1") int page,
                                   @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum,
                                   RecycleBin recycleBin) {
        return recycleBinService.getListByParam(page, pageNum, recycleBin);
    }


    /**
     * 批量彻底删除文件
     */
    @PostMapping("delete")
    public Result deleteAssets(String deleteJson) {
        recycleBinService.batchDelete(deleteJson);
        return Result.newSuccess();
    }

    /**
     * 批量还原文件
     */
    @PostMapping("restore")
    public Result restoreAssets(String projectUuid, String restoreData) {
        return recycleBinService.batchRestore(projectUuid, restoreData);
    }

    /**
     * 清空回收站
     */
    @PostMapping("empty")
    public Result empty(@RequestParam("projectUuid") String projectUuid) {
        recycleBinService.setEmpty(projectUuid);
        return Result.newSuccess();
    }

}
