package com.beagledata.gaea.workbench;

import com.beagledata.gaea.common.LicenseInterceptor;
import com.beagledata.gaea.common.SafelyShutdown;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.interceptor.DqcInterceptor;
import com.beagledata.gaea.workbench.interceptor.PasswordValidateInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by liulu on 2017/12/14.
 */
@Configuration
public class WebRegistrationBean extends WebMvcConfigurerAdapter implements EmbeddedServletContainerCustomizer {
    @Autowired
    private ResourceResolver resourceResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(licenseInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/api/**")
                .excludePathPatterns("/license/**")
                .excludePathPatterns("/error")
                .excludePathPatterns("/forceLogin")
                .excludePathPatterns("/rest/**");
        registry.addInterceptor(dqcInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/api/**")
                .excludePathPatterns("/license/**")
                .excludePathPatterns("/error")
                .excludePathPatterns("/forceLogin")
                .excludePathPatterns("/rest/**")
                .excludePathPatterns("/user/profile");
    }

    @Bean
    public LicenseInterceptor licenseInterceptor(){
        return new LicenseInterceptor(resourceResolver.getLicensePath());
    }

    @Bean
    public PasswordValidateInterceptor passwordValidateInterceptor(){
        return new PasswordValidateInterceptor();
    }

    @Bean
    public DqcInterceptor dqcInterceptor() {
        return new DqcInterceptor();
    }

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
