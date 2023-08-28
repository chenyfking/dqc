package com.beagledata.gaea.gateway.loadbalance;

import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-11-06
 */
@Configuration
@RibbonClients(defaultConfiguration = {CustomRibbonClientConfiguration.class})
public class CustomRibbonAutoConfiguration {
}