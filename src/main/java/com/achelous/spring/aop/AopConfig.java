package com.achelous.spring.aop;

import com.achelous.spring.transaction.TransactionManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: fanJiang
 * @Date: Create in 9:00 2018/5/5
 */

/**
 * 封装 aspect的信息    如@Before @After
 * 注解或配置文件的目的是告诉spring 那么方法需要使用aop增强   又如何增强
 */
public class AopConfig {

    // 需要aop增强的方法作为key  增强的方法作为value
    private Map<Method, Aspect> points = new HashMap<>();


    public void put(Method target, Object aspect, Method[] points) {
        this.points.put(target, new Aspect(aspect, points));
    }

    public void put(Method target, Object aspect, Method[] points, TransactionManager transactionManager) {
        this.points.put(target, new Aspect(aspect, points, transactionManager));
    }

    public Aspect get (Method method) {
        return this.points.get(method);
    }

    public boolean contais(Method method) {
        return this.points.containsKey(method);
    }

    // 对增强的代码封装
    public class Aspect {
        private Object aspect; //  对象的调用者  切面对象
        private Method [] points; //   @beginTransaction @after  增强方法
        private TransactionManager transactionManager;

        public Aspect(Object aspect, Method[] points) {
            this.aspect = aspect;
            this.points = points;
        }

        public Aspect(Object aspect, Method[] points, TransactionManager transactionManager) {
            this.aspect = aspect;
            this.points = points;
            this.transactionManager = transactionManager;
        }

        public TransactionManager getTransactionManager() {
            return transactionManager;
        }

        public Object getAspect() {
            return aspect;
        }

        public Method[] getPoints() {
            return points;
        }
    }

}
