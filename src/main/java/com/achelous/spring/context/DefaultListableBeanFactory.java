package com.achelous.spring.context;

import com.achelous.spring.beans.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: fanJiang
 * @Date: Create in 8:43 2018/5/5
 */
public class DefaultListableBeanFactory extends AbstractApplicationContext {


    // 用来保存 配置信息
    protected Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();



    @Override
    protected void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void refreshBeanFactory() {

    }
}
