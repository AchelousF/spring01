package com.achelous.spring.annotation;

import java.lang.annotation.*;

/**
 * @Auther: fanJiang
 * @Date: Create in 9:16 2018/5/12
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
}
