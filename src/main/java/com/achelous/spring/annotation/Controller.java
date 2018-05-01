package com.achelous.spring.annotation;

import java.lang.annotation.*;

/**
 * @Auther: fanJiang
 * @Date: Create in 10:55 2018/4/22
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

    String value() default "";
}
