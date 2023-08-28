package com.beagledata.gaea.ruleengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数传递方向
 *
 * Created by liulu on 2020/5/11.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PassingDirection {
    Direction value() default Direction.IN_OUT;

    enum Direction {
        IN_OUT, // 传入传出
        IN, // 仅传入
        OUT, // 仅传出
        NONE // 不传入不传出
    }
}
