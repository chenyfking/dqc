package com.beagledata.gaea.executionserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Created by Cyf on 2020/3/17
 **/
@SpringBootApplication
@ComponentScan(
        excludeFilters = {
                @ComponentScan.Filter (
                        type = FilterType.ASSIGNABLE_TYPE
                )
        }
)
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}