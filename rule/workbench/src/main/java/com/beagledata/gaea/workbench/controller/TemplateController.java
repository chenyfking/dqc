package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.AssetsTemplate;
import com.beagledata.gaea.workbench.service.AssetsTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 模板管理
 * Created by Chenyafeng on 2020/6/18
 */
@RestController
@RequestMapping("template")
public class TemplateController {
    @Autowired
    private AssetsTemplateService assetsTemplateService;


    /**
     * 文件转为模板
     * Created by Chenyafeng on 2020/6/18
     */
    @PostMapping("")
    public Result convert2Template(String assetsUuid, String name) {
        return assetsTemplateService.add(assetsUuid, name);
    }

    /**
     * 根据uuid查询模板
     */
    @GetMapping("{uuid}")
    public Result getTemplateByUuid(@PathVariable String uuid) {
        return Result.newSuccess().withData(assetsTemplateService.getByUuid(uuid));
    }

    /**
     * 根据类型、项目查询模板
     */
    @GetMapping("")
    public Result getTemplate(AssetsTemplate template) {
        return assetsTemplateService.getTemplates(template);
    }

    /**
     * 更新模板内容
     * Created by Chenyafeng on 2020/6/18
     */
    @PostMapping("content")
    public Result updateContent(String uuid, String content) {
        return assetsTemplateService.updateContentByUuid(uuid, content);
    }

    /**
     * 删除模板
     * Created by Chenyafeng on 2020/6/18
     */
    @PostMapping("delete")
    public Result delete(String uuid) {
        assetsTemplateService.delete(uuid);
        return Result.newSuccess();
    }
}
