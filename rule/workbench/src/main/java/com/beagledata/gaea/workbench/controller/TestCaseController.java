package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.service.AssetsService;
import com.beagledata.gaea.workbench.service.TestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: yinrj
 * @Date: 0022 2020/6/22 15:48
 * @Description: 测试案例
 */
@RestController
@RequestMapping("testcase")
public class TestCaseController {
    @Autowired
    private TestCaseService testCaseService;
    @Autowired
    private AssetsService assetsService;

    /**
     *@auto: yinrj
     *@Description:保存策测试案例
     *@Date: 2020-06-24 17:24
     **/
    @PostMapping("save")
    @RestLogAnnotation(describe = "保存测试案例")
    public Result saveTestCase(Assets assets, String ruleUuid, Integer assetsVersion) {
        assetsService.saveContent(assets);
        testCaseService.add(ruleUuid, assets.getUuid(), assetsVersion);
        return Result.newSuccess();
    }

    /**
     * @Author yangyongqiang
     * @Description 删除测试案例
     * @Date 9:57 上午 2020/7/1
     * @Param [caseUuid]
     * @return com.beagledata.common.Result
     **/
    @PostMapping("delete")
    @RestLogAnnotation(describe = "删除测试案例")
    public Result delete(String caseUuid) {
        testCaseService.delete(caseUuid);
        return Result.newSuccess();
    }
}
