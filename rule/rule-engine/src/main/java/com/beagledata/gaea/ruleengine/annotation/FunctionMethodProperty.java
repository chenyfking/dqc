package com.beagledata.gaea.ruleengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liulu on 2018/10/12.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionMethodProperty {
    /**
     * @return 函数名称
     */
    String name();

    /**
     * @return 参数列表
     */
    String[] params() default {};
}
