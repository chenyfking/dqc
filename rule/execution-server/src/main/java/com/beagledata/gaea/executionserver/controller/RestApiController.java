package com.beagledata.gaea.executionserver.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.RestConstants;
import com.beagledata.gaea.executionserver.service.RestApiService;
import com.beagledata.license.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对外提供REST API
 *
 * Created by liulu on 2020/5/13.
 */
@RestController
public class RestApiController {
    @Autowired
    private RestApiService restApiService;

    /**
     * @return 获取当前服务的序列号
     */
    @GetMapping(RestConstants.OpenApi.Endpoints.SERIALNUMBER)
    public Result getSerialNumber() {
        return Result.newSuccess().withData(Utils.getSn());
    }

    /**
     * 上传license
     *
     * @param license
     * @return
     */
    @PostMapping(RestConstants.OpenApi.Endpoints.UPLOAD_LICENSE)
    public Result uploadLicense(String license) {
        restApiService.uploadLicense(license);
        return Result.newSuccess();
    }

    /**
     * 发布服务
     *
     * @param file
     * @return
     */
    @PostMapping(RestConstants.OpenApi.Endpoints.DEPLOY)
    public Result deploy(@RequestBody MultipartFile file) {
        restApiService.deploy(file);
        return Result.newSuccess();
    }

    /**
     * 删除服务
     *
     * @param microId
     * @return
     */
    @PostMapping(RestConstants.OpenApi.Endpoints.UNDEPLOY)
    public Result unDeploy(String microId) {
        restApiService.unDeploy(microId);
        return Result.newSuccess();
    }

    /**
     * 服务调用文档
     *
     * @param uuid
     * @return
     */
    @GetMapping(RestConstants.OpenApi.Endpoints.APIDOC)
    public Result getApiDoc(String uuid) {
        return Result.newSuccess().withData(restApiService.getApiDoc(uuid));
    }
}
