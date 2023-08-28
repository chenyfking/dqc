package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by mahongfei on 2018/12/18.
 */
@RestController
@RequestMapping("history")
public class TestDataController {
    @Autowired
    private TestService testService;

    /**
    *@Author: mahongfei
    *@description: 查询仿真测试库的历史数据
    */
    @GetMapping("")
    public Result<List<Map>> historyDetails(@RequestParam(required = false, defaultValue = "1") int page,
                                            @RequestParam(required = false, defaultValue = Constants.PAGE_ROWS) int pageNum,
                                            String packageId, @RequestParam(defaultValue = "0") Integer baselineVersion) {
        return Result.newSuccess().withData(testService.listDataByProjectPackage(page, pageNum, packageId, baselineVersion));
    }

    /**
    *@Author:mahongfei
    *@description:删除仿真测试历史记录
    */
    @PostMapping("delete")
    @RestLogAnnotation(describe = "删除仿真测试历史记录")
    public Result deleteHistory(String uuid) {
        testService.deleteData(uuid);
        return Result.newSuccess();
    }
}
