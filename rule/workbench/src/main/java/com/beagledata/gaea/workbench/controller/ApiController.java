package com.beagledata.gaea.workbench.controller;

import com.alibaba.fastjson.JSONArray;
import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.service.*;
import com.beagledata.gaea.workbench.util.UserSessionManager;
import com.beagledata.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by liulu on 2018/6/25.
 */
@RestController
@RequestMapping("api")
public class ApiController {
    @Autowired
    private AiModelService aiModelService;
    @Autowired
    private MicroService microService;
    @Autowired
    private MicroRouteService microRouteService;
    @Autowired
    private KnowledgePackageService knowledgePackageService;
    @Autowired
    private AssetsService assetsService;
    @Autowired
    private UserSessionManager userSessionManager;
    @Autowired
    private ResourceResolver resourceResolver;
    @PostMapping("aimodelpredict")
    public Result predict(@RequestBody String json) {
        JSONArray result = aiModelService.predictByModelName(json);
        if (result != null) {
            return Result.newSuccess().withData(result);
        }
        return Result.newError();
    }

    /**
     * 在线部署模型
     * @param file 模型jar文件流
     * 参数：file 模型文件 , params 模型参数
     * 返回结果：code：0成功；-1失败；-3模型已经存在
     */
    @PostMapping("publishmodel")
    @ResponseBody
    public Result publishmodel(@RequestBody MultipartFile file,
                               @RequestParam(defaultValue = "",required = false) String params) {
        Result result = aiModelService.apiPublish(file, params);
        microRouteService.refreshMicroRouteToRedis();
        return result;
    }

    /**
     * 查询项目所有的知识包
     * @param uuid 项目uuid
     */
    @GetMapping("pkglist/{uuid}")
    public Result getPkgForPrj(@PathVariable("uuid") String uuid) {
        return Result.newSuccess().withData(knowledgePackageService.getPkgList(uuid));
    }

    /**
     * 下载知识包(第三方系统调用)
     * @param pkguuid 知识包uuid
     */
    @GetMapping("downpkg/{pkguuid}")
    public void downPkg(@PathVariable("pkguuid")String pkguuid, HttpServletResponse response) {
        knowledgePackageService.download(pkguuid, response);
    }

    /**
     * 下载知识包参数(第三方系统调用)
     * @param pkguuid 知识包uuid
     */
    @GetMapping("downparam/{pkguuid}")
    public void downPkgParam(@PathVariable("pkguuid")String pkguuid, HttpServletResponse response) {
        knowledgePackageService.downPkgFacts(pkguuid, response);
    }

    /**
     * 统计项目中不同类型文件的数量
     * @param uuid 项目uuid
     */
    @GetMapping("typenum/{uuid}")
    public Result getPrjTypeNum(@PathVariable("uuid") String uuid, @RequestParam(value = "types", required = false)String[] types) {
        if (null != types && types.length > 0) {
            Set<String> set = Arrays.stream(types).collect(Collectors.toSet());
            return Result.newSuccess().withData(assetsService.countNumByTypeForPrj(uuid, set));
        }
        return Result.newSuccess().withData(assetsService.countNumByTypeForPrj(uuid, null));
    }

    /**
     *自动创建数据模型
     */
    @PostMapping("autofact")
    public Result autoFact(@RequestBody String json) {
        return assetsService.autoCreateFact(json);
    }

    /**
     * 获得dqc首页登录地址
     */
    @GetMapping("dqcindex")
    public Result dqcIndex() {
        String url = resourceResolver.getDqcLoginUrl();
        if (StringUtils.isBlank(url)) {
            return Result.newError().withMsg("请打开数据质量工具登录页面进行登录");
        }
        return Result.newSuccess().withData(url);
    }

    /**
     * dqc主系统登录后向gaea同步添加用户标志
     */
    @PostMapping("dqcadd")
    public Result dqcadd(String info) {
        if (StringUtils.isBlank(info)) {
            return Result.newError().withMsg("用户标识为空");
        }
        if (userSessionManager.addDqcUserInfo(new String(Base64.getDecoder().decode(info)))) {
            return Result.newSuccess();
        }
        return Result.newError().withMsg("添加用户标识信息失败");
    }

    /**
     * dqc主系统退出后向gaea同步删除用户标志
     */
    @PostMapping("dqcdel")
    public Result dqcdel(String info) {
        if (StringUtils.isBlank(info)) {
            return Result.newError().withMsg("用户标识为空");
        }
        if (userSessionManager.delDqcUserInfo(new String(Base64.getDecoder().decode(info)))) {
            return Result.newSuccess();
        }
         return Result.newError().withMsg("删除用户标识信息失败");
    }
}
