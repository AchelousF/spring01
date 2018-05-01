package com.achelous.spring.beans;

/**
 * @Auther: fanJiang
 * @Date: Create in 20:29 2018/4/24
 */
public class BeanWrapper {

    //TODO 还会用到    观察者模式
    // 1. 支持事件响应， 会有监听
    private BeanPostProcessor postProcessor;


    private Object wrapperInstance;
    // 原始的    通过反射new出来的对象   要把其包装起来并保存
    private Object originalInstance;

    public BeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
        this.originalInstance = wrapperInstance;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public BeanPostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(BeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    /**
     * @return 返回代理以后的class
     */
    public Class<?> getWrapperClass() {
        return this.wrapperInstance.getClass();
    }



}
