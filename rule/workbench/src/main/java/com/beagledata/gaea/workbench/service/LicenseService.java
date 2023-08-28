package com.beagledata.gaea.workbench.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by liulu on 2020/11/14.
 */
public interface LicenseService {
    /**
     * 获取License序列号
     *
     * @return
     */
    String getSn();

    /**
     * 上传License
     */
    void upload(MultipartFile file);
}
