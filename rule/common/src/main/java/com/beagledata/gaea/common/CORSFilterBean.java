package com.beagledata.gaea.common;

import com.thetransactioncompany.cors.CORSFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liulu on 2020/5/20.
 */
@Configuration
@ConditionalOnClass(CORSFilter.class)
public class CORSFilterBean {
    /**
     * 注册跨域Filter
     */
    @Bean
    public FilterRegistrationBean corsFilterBean() {
        Map<String, String> properties = new HashMap<>();
        properties.put("cors.allowGenericHttpRequests", "true");
        properties.put("cors.allowOrigin", "*");
        properties.put("cors.allowSubdomains", "false");
        properties.put("cors.supportedMethods", "GET, HEAD, POST, OPTIONS");
        properties.put("cors.supportedHeaders", "Accept, Origin, X-Requested-With, Content-Type, Last-Modified");
        properties.put("cors.supportsCredentials", "true");
        properties.put("cors.maxAge", "3600");
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new CORSFilter());
        filterRegistrationBean.setInitParameters(properties);
        filterRegistrationBean.setName("CORS");
        filterRegistrationBean.setUrlPatterns(Collections.singletonList("/*"));
        return filterRegistrationBean;
    }
}
