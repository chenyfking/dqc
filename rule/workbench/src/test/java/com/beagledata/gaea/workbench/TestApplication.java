package com.beagledata.gaea.workbench;

import com.beagledata.gaea.workbench.service.ProjectService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Created by liulu on 2017/12/19.
 */
@SpringBootApplication
@ComponentScan(
        excludeFilters = {
                @ComponentScan.Filter (
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = ProjectService.class
                )
        }
)
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
