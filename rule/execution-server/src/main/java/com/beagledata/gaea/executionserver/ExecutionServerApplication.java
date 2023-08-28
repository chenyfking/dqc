package com.beagledata.gaea.executionserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author chenyafeng
 * @date 2018/12/28
 */
@SpringBootApplication
@ComponentScan("com.beagledata.gaea")
@EnableScheduling
public class ExecutionServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(ExecutionServerApplication.class, args);
	}
}

