package com.achelous.spring.beans;

/**
 * @Auther: fanJiang
 * @Date: Create in 21:35 2018/4/24
 */
public class BeanPostProcessor {

    public Object postPrecessBeforeInitialization(Object obj, String name){
        return null;
    }

    public Object postPrecessAfterInitialization(Object obj, String name){
        return null;
    }
}
