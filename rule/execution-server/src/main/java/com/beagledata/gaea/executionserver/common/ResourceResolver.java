package com.beagledata.gaea.executionserver.common;

import com.beagledata.gaea.executionserver.config.DefaultConfigs;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

/**
 * Created by liulu on 2019/7/23.
 */
@Component
public class ResourceResolver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DefaultConfigs configs;
    @Autowired
    private MultipartProperties multipartProperties;

    @PostConstruct
    public void init() {
        try {
            FileUtils.forceMkdir(SafelyFiles.newFile(multipartProperties.getLocation()));
            FileUtils.forceMkdir(SafelyFiles.newFile(getDecisionLogPath()));
            FileUtils.forceMkdir(SafelyFiles.newFile(getTmpDirPath()));

        } catch (IOException e) {
            logger.error("初始化目录失败", e);
        }
    }


    /**
     * @return 获取license路径
     */
    public String getLicensePath() {
        return SafelyFiles.newFile(configs.getDataHome(), "license/license").getAbsolutePath();
    }

    /**
     * @return 获取决策日志存储路径
     */
    public String getDecisionLogPath() {
        return SafelyFiles.newFile(configs.getDataHome(), "decision_log").getAbsolutePath();
    }

    /**
    * 描述: 临时目录
    * @param: []
    * @author: 周庚新
    * @date: 2020/11/9
    * @return: java.lang.String
    */
    public String getTmpDirPath() {
        return SafelyFiles.newFile(configs.getDataHome(), "tmp").getAbsolutePath();
    }
}
