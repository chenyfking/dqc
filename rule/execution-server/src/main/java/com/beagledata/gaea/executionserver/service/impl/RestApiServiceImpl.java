package com.beagledata.gaea.executionserver.service.impl;

import com.beagledata.gaea.executioncore.MicroExecutor;
import com.beagledata.gaea.executioncore.domain.ApiDoc;
import com.beagledata.gaea.executioncore.domain.Micro;
import com.beagledata.gaea.executioncore.domain.MicroFactory;
import com.beagledata.gaea.executioncore.handler.ModelHandler;
import com.beagledata.gaea.executioncore.handler.ModelHandlerFactory;
import com.beagledata.gaea.executionserver.common.ResourceResolver;
import com.beagledata.gaea.executionserver.service.RestApiService;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.license.License;
import com.beagledata.license.LicenseException;
import com.beagledata.license.LicenseTools;
import com.beagledata.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * Created by liulu on 2020/5/13.
 */
@Service
public class RestApiServiceImpl implements RestApiService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ResourceResolver resourceResolver;
    @Autowired
    private MicroExecutor microExecutor;

    @Override
    public void uploadLicense(String licenseStr) {
        if (StringUtils.isBlank(licenseStr)) {
            logger.warn("上传license失败, license: {}", licenseStr);
            throw new IllegalArgumentException("license无效");
        }

        try {
            FileUtils.writeStringToFile(new File(resourceResolver.getLicensePath()), licenseStr);
            License license = new LicenseTools().getLicense(resourceResolver.getLicensePath());
            license.verify();
        } catch (LicenseException e) {
            logger.warn("license无效: {}", licenseStr);
            throw new IllegalArgumentException("license无效");
        } catch (Exception e) {
            logger.warn("上传license失败: " + e.getLocalizedMessage(), e);
            throw new IllegalStateException("上传license失败");
        }
    }

    @Override
    public void deploy(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("服务上线失败，文件找不到");
        }
        if (!file.getOriginalFilename().endsWith(".zip")) {
            logger.warn("服务上线失败，文件格式不支持: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("服务上线失败，文件格式不支持");
        }
        
        try {
            microExecutor.load(file.getInputStream());
        } catch (IllegalArgumentException | RuleException e) {
            throw e;
        } catch (Exception e) {
            logger.error("服务上线失败. file: {}", file.getOriginalFilename(), e);
            throw new IllegalStateException("服务上线失败");
        }
    }

    @Override
    public void unDeploy(String microUuid) {
        microExecutor.unload(microUuid);
    }

    @Override
    public ApiDoc getApiDoc(String microUuid) {
        Micro micro = MicroFactory.get(microUuid);
        if (micro == null) {
            throw new IllegalArgumentException("服务找不到，请检查参数");
        }

        String modelUuid = micro.getDeployment().getModels().stream().filter(model -> model.isPrimary()).findFirst().get().getId();
        ModelHandler handler = ModelHandlerFactory.get(modelUuid);
        if (handler == null) {
            throw new IllegalArgumentException("服务找不到，请检查参数");
        }

        return handler.getApiDoc();
    }
}
