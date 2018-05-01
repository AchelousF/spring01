package com.achelous.spring.annotation;

import java.lang.annotation.*;

/**
 * @Auther: fanJiang
 * @Date: Create in 10:57 2018/4/22
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    String value() default "";
}
