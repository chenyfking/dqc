package com.beagledata.gaea.executionserver.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.executionserver.service.ExecuteService;
import com.beagledata.license.Utils;
import com.beagledata.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用接口
 * Created by Chenyafeng on 2019/6/11.
 */
@RestController
public class CommonController {
    private static Logger logger = LoggerFactory.getLogger(CommonController.class);
    @Autowired
    private ExecuteService apiService;

    /**
     * 心跳监测，测试openapi服务是否有效
     */
    @GetMapping("heart")
    public Result heart() {
        return Result.newSuccess();
    }

    /**
     * 获得license序列号
     */
    @GetMapping("getlicense")
    public String getLicense() {
        try {
            return Utils.getSn();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * 上传license文件
     * 服务器调用接口示例：curl -F "key=value" -F "file=@license"  http://192.168.100.19:33006/openapi/upload
     */
    @PostMapping("upload")
    public Result upload(@RequestBody MultipartFile file) throws Exception {
        return apiService.uploadLicense(file.getInputStream());
    }
}
