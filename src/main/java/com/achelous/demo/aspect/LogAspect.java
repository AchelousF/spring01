package com.achelous.demo.aspect;

/**
 * @Auther: fanJiang
 * @Date: Create in 8:57 2018/5/5
 */
public class LogAspect {


    // 在调用方法前  执行before方法
    public void before() {
        System.out.println("invoke beginTransaction");
    }

    // 在调用方法后  执行after方法
    public void after() {
        System.out.println("invoke after");
    }
}
