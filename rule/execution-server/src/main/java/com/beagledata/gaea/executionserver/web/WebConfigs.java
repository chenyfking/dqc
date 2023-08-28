package com.beagledata.gaea.executionserver.web;

import com.beagledata.gaea.common.SafelyShutdown;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Cyf on 2019/7/9
 **/
@Configuration
public class WebConfigs extends WebMvcConfigurerAdapter implements EmbeddedServletContainerCustomizer {
// 暂时注释License校验
//    @Autowired
//    private ResourceResolver resourceResolver;

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(licenseInterceptor())
//                .addPathPatterns("/**")
//                .excludePathPatterns("/upload")
//                .excludePathPatterns("/getlicense")
//                .excludePathPatterns("/aimodel/**")
//                .excludePathPatterns("/monitor/**")
//                .excludePathPatterns("/rest/**");
//    }

//    @Bean
//    public LicenseInterceptor licenseInterceptor(){
//        return new LicenseInterceptor(resourceResolver.getLicensePath());
//    }

    @Bean
    public SafelyShutdown safelyShutdown() {
        return new SafelyShutdown();
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        TomcatEmbeddedServletContainerFactory factory = (TomcatEmbeddedServletContainerFactory)container;
        factory.addConnectorCustomizers(safelyShutdown());
    }
}