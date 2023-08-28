package com.beagledata.gaea.gateway;

import com.beagledata.gaea.gateway.filter.postrequest.RibbonPerClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * Created by liulu on 2020/6/28.
 */
@RibbonClients(defaultConfiguration = RibbonPerClientConfiguration.class)
@SpringBootApplication
@EnableZuulProxy
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
