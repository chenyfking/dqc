package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.KnowledgePackage;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.KnowledgePackageService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by mahongfei on 2018/10/8.
 */
@RestController
@RequestMapping("knowledgepackage")
public class KnowledgePackageController {
    @Autowired
    private KnowledgePackageService knowledgePackageService;

    @PostMapping("add")
    @RequiresPermissions(PermissionSet.Project.CODE_PKG_ADD)
    @RestLogAnnotation(describe = "添加知识包")
    public Result addKnowledgePackage(KnowledgePackage knowledgePackage) {
        knowledgePackageService.addKnowledgePackage(knowledgePackage);
        return Result.newSuccess();
    }

    @PostMapping("edit")
    @RequiresPermissions(PermissionSet.Project.CODE_PKG_EDIT)
    @RestLogAnnotation(describe = "编辑知识包")
    public Result editKnowledgePackage(KnowledgePackage knowledgePackage) {
        knowledgePackageService.editKnowledgePackage(knowledgePackage);
        return Result.newSuccess();
    }

    /**
     *@Author: mahongfei
     *@description: 知识包列表
     */
    @GetMapping("")
    public Result<List<KnowledgePackage>> listAll(String projectUuid){
       return Result.newSuccess().withData(knowledgePackageService.listAll(projectUuid));
    }

    /**
     *@Author: mahongfei
     *@description:  删除知识包
     */
    @PostMapping("delete")
    @RestLogAnnotation(describe = "删除知识包")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_DEL)
    public Result deleteknowledgePackage(String uuid) {
        return knowledgePackageService.deleteknowledgePackage(uuid);
    }

     /**
      *@auto: yangyongqiang
      *@Description:知识包添加资源文件
      *@Date: 2018-10-08 14:21
      **/
    @PostMapping("assets")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_EDIT)
    @RestLogAnnotation(describe = "添加知识包资源文件")
    public Result addKnowledgePackageAssets(KnowledgePackage knowledgePackage) {
        knowledgePackageService.addKnowledgePackageAssets(knowledgePackage);
        return Result.newSuccess();
    }

    /**
     *@auto: yangyongqiang
     *@Description:知识包删除资源文件
     *@Date: 2018-10-08 14:21
     **/
    @PostMapping("assets/delete")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_EDIT)
    @RestLogAnnotation(describe = "删除知识包资源文件")
    public Result deleteKnowledgePackageAssets(KnowledgePackage knowledgePackage) {
        knowledgePackageService.deleteKnowledgePackageAssets(knowledgePackage);
        return Result.newSuccess();
    }

    /**
     *@auto: yangyongqiang
     *@Description:知识包查询资源文件列表
     *@Date: 2018-10-08 14:21
     **/
    @GetMapping("assets")
    public Result selectKnowledgePackageAssets(KnowledgePackage knowledgePackage) {
        return Result.newSuccess().withData(knowledgePackageService.selectKnowledgePackageAssets(knowledgePackage));
    }

    /**
     * @author liulu
     * 2018/10/8 下午 05:45
     */
    @GetMapping("testdata")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_RUN)
    public Result getTestData(String uuid, @RequestParam(defaultValue = "0") Integer baselineVersion) {
        return Result.newSuccess().withData(knowledgePackageService.getTestData(uuid, baselineVersion));
    }

    /**
     * @author liulu
     * 2018/10/8 下午 06:38
     */
    @PostMapping("test")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_RUN)
    public Result test(String uuid, String facts, @RequestParam(defaultValue = "0") Integer baselineVersion) {
        return Result.newSuccess().withData(knowledgePackageService.test(uuid, facts, baselineVersion));
    }

    /**
     * @author liulu
     * 2018/10/10 下午 05:47
     */
    @GetMapping("download")
    @RequiresPermissions(value = PermissionSet.Project.CODE_BASELINE_DOWNLOAD)
    @RestLogAnnotation(describe = "下载知识包基线")
    public void download(String uuid, @RequestParam(defaultValue = "0") Integer baselineVersion, HttpServletResponse response) {
        knowledgePackageService.download(uuid, baselineVersion, response);
    }

    @PostMapping("upload")
    @RequiresPermissions(PermissionSet.Micro.CODE_MICRO_IMPORTBASELINE)
    public Result uploadPackage(@RequestBody MultipartFile file) {
        return knowledgePackageService.upload(file);
    }

    /**
     * 下载批量测试使用的模板
     * @author chenyafeng
     * @date 2018/11/7
     */
    @GetMapping("batchmodel")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_RUN)
    @RestLogAnnotation(describe = "下载知识包批量测试使用的模板")
    public void downbatchmodel(String uuid, @RequestParam(defaultValue = "0") Integer baselineVersion, HttpServletResponse response) {
        knowledgePackageService.downBatchModel(uuid, baselineVersion, response);
    }

    /**
     * 上传批量测试的数据
     * @author chenyafeng
     * @date 2018/11/19
     */
    @PostMapping("importbatchdata")
    @RestLogAnnotation(describe = "上传知识包批量测试的数据")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_RUN)
    public Result importbatchdata(String uuid, MultipartFile file) {
        return knowledgePackageService.importBatchData(uuid, file);
    }

    /**
     * 批量测试
     * @author chenyafeng
     * @date 2018/11/20
     */
    @PostMapping("batchtest")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_RUN)
    @RestLogAnnotation(describe = "知识包批量测试")
    public void batchTest(String uuid, @RequestBody MultipartFile file, @RequestParam(defaultValue = "0") Integer baselineVersion, HttpServletResponse response) {
        knowledgePackageService.batchTest(uuid, baselineVersion, file, response);
    }

    /**
     * 下载批量测试结果数据
     * Created by Chenyafeng on 2020/3/23
     */
    @PostMapping("exportresult")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_RUN)
    public Result exportBatchResult(HttpServletResponse response, @RequestBody String json) {
        knowledgePackageService.exportBatchResult(response, json);
        return Result.newSuccess();
    }

    /**
     * 提交发布审核
     * @author chenyafeng
     * @date 2018/12/4
     */
    @PostMapping("audit")
    public Result submitAudit(String packageUuid) {
        return knowledgePackageService.submitAudit(packageUuid);
    }

    /**
     * 创建临时知识包
     *
     * @author liulu
     * 2019/5/21 17:13
     */
    @PostMapping("temptest")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_RUN)
    public Result tempTest(String assetsUuid, @RequestParam(defaultValue = "0") Integer assetsVersion) {
        return Result.newSuccess().withData(knowledgePackageService.addTempPkg(assetsUuid, assetsVersion));
    }

    /**
     * 下载知识包相关Fact对象文件
     */
    @GetMapping("downfacts")
    @RequiresPermissions(value = PermissionSet.Project.CODE_PKG_RUN)
    @RestLogAnnotation(describe = "下载知识包相关数据模型")
    public void downFacts(String pkguuid, HttpServletResponse response) {
        knowledgePackageService.downPkgFacts(pkguuid, response);
    }
}
