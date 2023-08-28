package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.workbench.entity.TestData;
import com.beagledata.gaea.workbench.mapper.TestDataMapper;
import com.beagledata.gaea.workbench.service.TestService;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Created by liulu on 2018/1/25.
 */
@Service
public class TestServiceImpl implements TestService {
    private static Logger logger = LogManager.getLogger(TestService.class);

    @Autowired
    private TestDataMapper testDataMapper;

    @Override
    public List<TestData> listDataByProjectPackage(int page, int pageNum, String packageId, Integer baselineVersion) {
        try {
            return testDataMapper.selectByProjectPackage((page - 1) * pageNum, pageNum, packageId, baselineVersion);
        } catch (Exception e) {
            logger.error(packageId + e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public void addData(TestData testData) {
        if (StringUtils.isNotBlank(testData.getData())
				&& StringUtils.isNotBlank(testData.getPackageId())) {
            testData.setUuid(IdUtils.UUID());
            try {
                testDataMapper.insert(testData);
            } catch (Exception e) {
                logger.error(testData + e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void deleteData(String uuid) {
        try {
            testDataMapper.delete(uuid);
        } catch (Exception e) {
            logger.error(uuid + e.getLocalizedMessage(), e);
        }
    }
}
