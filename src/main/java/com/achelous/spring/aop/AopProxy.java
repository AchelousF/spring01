package com.achelous.spring.aop;

/**
 * @Auther: fanJiang
 * @Date: Create in 8:49 2018/5/5
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 这里默认使用jdk 动态代理   (暂不考虑cglib)
 */
public class AopProxy implements InvocationHandler {

    private AopConfig config;

    private Object target;


    public void setConfig(AopConfig config) {
        this.config = config;
    }

    //
    public Object getProxy(Object instance){
        this.target = instance;
        Class<?> clazz = instance.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 这里的method方法的代理类的方法   使用target找到其原始method
        Method m = this.target.getClass().getMethod(method.getName(), method.getParameterTypes());

        // 调用before 方法 使用原始method进行获取
        if (config.contais(m)) {
            AopConfig.Aspect aspect = config.get(m);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }
        // 反射调用原始方法
        Object obj = method.invoke(this.target, args);

        // 调用after方法
        if (config.contais(m)) {
            AopConfig.Aspect aspect = config.get(m);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }
        return obj;
    }
}
