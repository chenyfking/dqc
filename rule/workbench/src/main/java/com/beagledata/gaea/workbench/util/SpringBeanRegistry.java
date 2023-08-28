package com.beagledata.gaea.workbench.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * Spring运行期动态注册bean到容器
 *
 * Created by liulu on 2018/1/17.
 */
@Component
public class SpringBeanRegistry implements BeanDefinitionRegistryPostProcessor {
    private BeanDefinitionRegistry beanDefinitionRegistry;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }

    /**
     * @param beanDefinition
     */
    public void registry(BeanDefinition beanDefinition) {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClass(beanDefinition.getBeanClass());
        definition.setScope(beanDefinition.getScope());
        definition.setLazyInit(beanDefinition.isLazyInit());
        definition.setAutowireCandidate(beanDefinition.isAutowireCandidate());
        beanDefinitionRegistry.registerBeanDefinition(beanDefinition.getBeanName(), definition);
    }

    public static class BeanDefinition {
        public static final String SCOPE_SINGLETON = "singleton";
        public static final String SCOPE_PROTOTYPE = "singleton";

        /**
         * Spring Bean Name
         */
        private String beanName;
        /**
         * Spring Bean Class
         */
        private Class beanClass;
        /**
         * Spring Bean Scope
         */
        private String scope = SCOPE_SINGLETON;
        /**
         * 是否懒加载
         */
        private boolean lazyInit = false;
        /**
         * 是否可以被其他对象自动注入
         */
        private boolean autowireCandidate = true;

        public BeanDefinition() {
        }

        public BeanDefinition(String beanName, Class beanClass) {
            this.beanName = beanName;
            this.beanClass = beanClass;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

        public Class getBeanClass() {
            return beanClass;
        }

        public void setBeanClass(Class beanClass) {
            this.beanClass = beanClass;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public boolean isLazyInit() {
            return lazyInit;
        }

        public void setLazyInit(boolean lazyInit) {
            this.lazyInit = lazyInit;
        }

        public boolean isAutowireCandidate() {
            return autowireCandidate;
        }

        public void setAutowireCandidate(boolean autowireCandidate) {
            this.autowireCandidate = autowireCandidate;
        }
    }
}
