package com.achelous.spring.beans;

/**
 * @Auther: fanJiang
 * @Date: Create in 20:28 2018/4/24
 */


/**
 * 用来存储配置文件中的bean信息
 * 内存中的配置  例如 beanID等
 */
public class BeanDefinition {


    private String beanClassName;

    private String factoryBeanName;

    private boolean lazyInit;


    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }
}
