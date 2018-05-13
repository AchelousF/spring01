package com.achelous.spring.aop;

/**
 * @Auther: fanJiang
 * @Date: Create in 8:49 2018/5/5
 */

import com.achelous.spring.annotation.Transactional;
import com.achelous.spring.transaction.TransactionManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

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
        before(m);
        // 反射调用原始方法
        Object obj = null;
        try {
            obj = method.invoke(this.target, args);
        } catch (Exception e) {
            // 调用rollback 方法    里面判断是否有当前事务   若有当前事务执行rollback  否则不做任何操作
            rollback(m);
            return obj;
        }
        // 调用after方法
        after(m);
        return obj;
    }

    // 当前是否存在事务   若存在执行事务的commit方法
    private void after(Method m) throws Exception {
        if (config.contais(m)) {
            AopConfig.Aspect aspect = config.get(m);
            if (aspect.getTransactionManager() != null) {
                aspect.getTransactionManager().commit();
            }
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }
    }

    private void rollback(Method m) throws SQLException {
        if (config.contais(m)) {
            AopConfig.Aspect aspect = config.get(m);
            if (aspect.getTransactionManager() != null) {
                aspect.getTransactionManager().rollback();
            }
        }
    }

    // TODO   这里仅仅只是对事务中的connection 进行了相应才操作    该connection 如何传入到方法中 使方法中的connection与此处是同一个对象
    // TODO   接入orm 框架优化
    private void before(Method method) throws Exception {
        if (config.contais(method)) {
            AopConfig.Aspect aspect = config.get(method);
            if (aspect.getTransactionManager() != null) {
                aspect.getTransactionManager().beginTransaction();
            }
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }
    }
}
