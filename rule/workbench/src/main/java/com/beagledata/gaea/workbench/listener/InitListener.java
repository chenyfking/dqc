package com.beagledata.gaea.workbench.listener;

import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.ruleengine.runtime.RuleContext;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.service.AiModelService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

/**
 * Created by liulu on 2017/12/18.
 */
@WebListener
public class InitListener implements ServletContextListener {
    private static Logger logger = LogManager.getLogger(InitListener.class);

    @Autowired
    private AiModelService aiModelService;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private ResourceResolver resourceResolver;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("初始化AI模型...");
        aiModelService.initModelAction();
        logger.info("初始化AI模型完成");

        RuleContext.setDataSource(dataSource);
        System.setProperty("gaea.tmpdir", resourceResolver.getTmpDirPath());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
