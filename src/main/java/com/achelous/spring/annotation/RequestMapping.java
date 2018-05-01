package com.achelous.spring.annotation;

import java.lang.annotation.*;

/**
 * @Auther: fanJiang
 * @Date: Create in 10:56 2018/4/22
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    String value() default "";
}
