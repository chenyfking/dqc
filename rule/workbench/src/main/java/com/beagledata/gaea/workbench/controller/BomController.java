package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.service.AssetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by liulu on 2018/9/29.
 */
@RestController
@RequestMapping("bom")
public class BomController {
    @Autowired
    private AssetsService assetsService;

    /**
     * @author liulu
     * 2018/9/29 下午 04:01
     */
    @GetMapping("")
    public Result<Map> listByProject(String projectUuid) {
        return Result.newSuccess().withData(assetsService.listBomByProjectUuid(projectUuid));
    }
}
