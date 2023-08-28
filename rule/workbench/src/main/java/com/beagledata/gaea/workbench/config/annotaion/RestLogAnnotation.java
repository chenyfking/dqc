package com.beagledata.gaea.workbench.config.annotaion;

import java.lang.annotation.*;

/**
 * @Auther: yinrj
 * @Date: 0020 2018/9/20 18:02
 * @Description: 日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RestLogAnnotation {
    /**
     * 日志注解-请求路径
     */
    String requestUrl() default "";

    /**
     *日志注解-方法描述
     */
    String describe();
}
