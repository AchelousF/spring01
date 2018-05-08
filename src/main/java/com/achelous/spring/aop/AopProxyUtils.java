package com.achelous.spring.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * @Auther: fanJiang
 * @Date: Create in 10:52 2018/5/6
 */
public class AopProxyUtils {

    public static Object getTargetObject(Object proxy) throws Exception {
        // 判断proxy 是否是代理对象   若不是直接返回

        return isAopProxy(proxy)? getProxyTargetObject(proxy) : proxy;
    }

    private static boolean isAopProxy(Object object) {
        return Proxy.isProxyClass(object.getClass());
    }

    private static Object getProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");

        h.setAccessible(true);

        AopProxy aopProxy = (AopProxy) h.get(proxy);

        Field target = aopProxy.getClass().getDeclaredField("target");

        target.setAccessible(true);
        return target.get(aopProxy);
    }
}
