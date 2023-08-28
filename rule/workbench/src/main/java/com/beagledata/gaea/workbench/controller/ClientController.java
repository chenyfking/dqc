package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.Client;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.service.ClientService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * yangyongqiang 2019/06/11
 */
@RestController
@RequestMapping("client")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @GetMapping("")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_CLIENT_VIEW)
    public Result listAll() {
        return Result.newSuccess().withData(clientService.listAll());
    }

    @PostMapping("add")
    @RestLogAnnotation(describe = "新增集群节点")
    public Result add(Client client) {
        clientService.add(client);
        return Result.newSuccess();
    }

    @PostMapping("update")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_CLIENT_EDIT)
    @RestLogAnnotation(describe = "编辑集群节点")
    public Result update(Client client) {
        clientService.update(client);
        return Result.newSuccess();
    }

    @PostMapping("enable")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_CLIENT_EDIT)
    @RestLogAnnotation(describe = "启用集群节点")
    public Result enable(String uuid) {
        clientService.enable(uuid);
        return Result.newSuccess();
    }

    @PostMapping("disable")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_CLIENT_EDIT)
    @RestLogAnnotation(describe = "禁用集群节点")
    public Result disable(String uuid) {
        clientService.disable(uuid);
        return Result.newSuccess();
    }

    @PostMapping("delete")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_CLIENT_DEL)
    @RestLogAnnotation(describe = "删除集群节点")
    public Result deleted(String uuid) {
        clientService.delete(uuid);
        return Result.newSuccess();
    }

    @GetMapping("micro/{uuid}")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_CLIENT_VIEW)
    public Result runningMicro(@PathVariable String uuid) {
        return clientService.getRunningMicro(uuid);
    }

    /**
     * 查看服务上线的集群节点
     * Created by Chenyafeng on 2019/6/24
     */
    @GetMapping("bymicro/{uuid}")
    @RequiresPermissions(value = PermissionSet.Micro.CODE_MICRO_CLIENT)
    public Result getClientsByMicro(@PathVariable String uuid) {
        return clientService.getClientsByMicro(uuid);
    }

    /**
     * 上传集群节点的license
     * Created by Chenyafeng on 2019/12/18
     */
    @PostMapping("putlicense")
    @RestLogAnnotation(describe = "上传集群节点的license")
    public Result putLicense(String uuid, @RequestBody MultipartFile file) {
        return clientService.uploadLicense(uuid, file);
    }

    /**
     * @return 获取所有可以发布的集群节点
     */
    @GetMapping("availables")
    public Result listForDeploy() {
        return Result.newSuccess().withData(clientService.listAvailables());
    }
}
