package com.achelous.spring.context;

/**
 * @Auther: fanJiang
 * @Date: Create in 8:40 2018/5/5
 */
public abstract class AbstractApplicationContext {


    protected void onRefresh() {

    }


    protected abstract void refreshBeanFactory();
}
