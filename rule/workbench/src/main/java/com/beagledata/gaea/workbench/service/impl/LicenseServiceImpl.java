package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.service.LicenseService;
import com.beagledata.license.License;
import com.beagledata.license.LicenseException;
import com.beagledata.license.LicenseTools;
import com.beagledata.license.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

/**
 * Created by liulu on 2020/11/14.
 */
@Service
public class LicenseServiceImpl extends BaseServiceImpl implements LicenseService {
    @Autowired
    private ResourceResolver resourceResolver;

    @Override
    public String getSn() {
        try {
            return Utils.getSn();
        } catch (Exception e) {
            logger.error("获取License序列号失败", e);
            throw new IllegalStateException("获取License序列号失败");
        }
    }

    @Override
    public void upload(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            FileUtils.writeByteArrayToFile(new File(resourceResolver.getLicensePath()), IOUtils.toByteArray(is));

            LicenseTools licenseTools = new LicenseTools();
			License license = licenseTools.getLicense(resourceResolver.getLicensePath());
			license.verify();
        } catch (LicenseException e) {
            if (LicenseException.CODE_EXPIRED == e.getCode()) {
                throw new IllegalStateException("License已过期，请重新上传");
            }
            throw new IllegalStateException("License不正确，请重新上传");
        } catch (Exception e) {
            logger.error("上传License失败: {}", file, e);
            throw new IllegalStateException("上传失败");
        }
    }
}
