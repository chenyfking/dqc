package com.beagledata.gaea.executionserver.service;

import com.beagledata.gaea.executioncore.domain.ApiDoc;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by liulu on 2020/5/13.
 */
public interface RestApiService {
    /**
     * 上传license
     *
     * @param licenseStr
     */
    void uploadLicense(String licenseStr);

    /**
     * 发布服务
     *
     * @param file
     */
    void deploy(MultipartFile file);

    /**
     * 删除服务
     *
     * @param microUuid
     */
    void unDeploy(String microUuid);

    /**
     * 获取服务API调用文档
     *
     * @param microUuid 服务uuid
     * @return
     */
    ApiDoc getApiDoc(String microUuid);
}
