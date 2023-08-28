package com.beagledata.gaea.gateway;

import com.beagledata.gaea.common.SafelyShutdown;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by liulu on 2017/12/14.
 */
@Configuration
public class WebRegistrationBean extends WebMvcConfigurerAdapter implements EmbeddedServletContainerCustomizer {
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
