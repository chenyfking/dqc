package com.beagledata.gaea.workbench.common;

import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.workbench.config.DefaultConfigs;
import com.beagledata.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

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
            FileUtils.forceMkdir(SafelyFiles.newFile(getModelPath()));
            FileUtils.forceMkdir(SafelyFiles.newFile(getFunctionPath()));
            FileUtils.forceMkdir(SafelyFiles.newFile(getDeployPath()));
            FileUtils.forceMkdir(SafelyFiles.newFile(getModelClassPath()));
            FileUtils.forceMkdir(SafelyFiles.newFile(getPkgBaselinePath()));
            FileUtils.forceMkdir(SafelyFiles.newFile(getTmpDirPath()));
        } catch (Exception e) {
            logger.error("初始化目录失败", e);
        }
    }

    /**
     * @return 获取license路径
     */
    public String getLicensePath() {
        return SafelyFiles.newFile(configs.getDataHome(), "license").getAbsolutePath();
    }

    /**
     * AI模型存储路径
     */
    public String getModelPath() {
        return SafelyFiles.newFile(configs.getDataHome(), "aimodel").getAbsolutePath();
    }

    /**
     * 用户自定义函数
     */
    public String getFunctionPath() {
        return SafelyFiles.newFile(configs.getDataHome(), "function").getAbsolutePath();
    }

    /**
    * 描述: 上线缓存路径
    * @param: []
    * @author: 周庚新
    * @date: 2020/11/11
    * @return: java.lang.String
    */
    public String getDeployPath() {
        return SafelyFiles.newFile(configs.getDataHome(), "deployment").getAbsolutePath();
    }

    /**
    * 描述: 上传java 数据模型文件临时存储路径
    * @param: []
    * @author: 周庚新
    * @date: 2020/11/11
    * @return: java.lang.String
    */
    public String getModelClassPath() {
        return SafelyFiles.newFile(configs.getDataHome(), "modelclassfile").getAbsolutePath();
    }

    /**
    * 描述: 上线导入知识包基线
    * @param: []
    * @author: 周庚新
    * @date: 2020/11/11
    * @return: java.lang.String
    */
    public String getPkgBaselinePath() {
        return SafelyFiles.newFile(configs.getDataHome(), "pkgBaseline").getAbsolutePath();
    }

    /**
    * 描述: 临时目录
    * @param: []
    * @author: 周庚新
    * @date: 2020/11/11
    * @return: java.lang.String
    */
    public String getTmpDirPath() {
        return SafelyFiles.newFile(configs.getDataHome(), "tmp").getAbsolutePath();
    }

    /**
     * 是否开启dqc会话控制
     */
    public boolean isOpenDqcSession() {
        if (StringUtils.isBlank(configs.getDqcsession())) {
            return false;
        }
        return "true".equalsIgnoreCase(configs.getDqcsession().trim());
    }

    /**
     * 获得dqc登录地址url
     */
    public String getDqcLoginUrl() {
        return configs.getDqcurl();
    }
}
