package com.beagledata.gaea.workbench;

import com.beagledata.common.SpringBeanHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by liulu on 2017/12/5.
 */
@SpringBootApplication
@ComponentScan("com.beagledata")
@ServletComponentScan
@EnableScheduling
public class WorkbenchApplication {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(WorkbenchApplication.class, args);
        SpringBeanHolder.initApplicationContext(ctx);
    }
}
