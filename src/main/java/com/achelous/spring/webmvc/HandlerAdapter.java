package com.achelous.spring.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

/**
 * @Auther: fanJiang
 * @Date: Create in 15:18 2018/4/29
 */
public class HandlerAdapter {


    private Map<String, Integer> paramMapping;


    public HandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    /**
     *
     * @param req 获取请求参数
     * @param resp 为了参数赋值
     * @param handle handle中包含了controller、method及url信息。
     * @return
     */
    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handle) throws InvocationTargetException, IllegalAccessException {
        // 根据用户请求的参数信息和method中的参数信息进行动态匹配。

        // 方法返回的modelAndView为空时      new一个ModelAndView

        // 1. 准备好方法的形参列表
        Class<?>[] parameterTypes = handle.getMethod().getParameterTypes();

        // 2. 拿到自定命名参数所在的位置
        // 用户请求的参数列表
        Map<String, String [] > parameterMap = req.getParameterMap();

        // 3. 构造实参列表
        Object[] paramValues = new Object[parameterMap.size()];

        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[\\]", "").replaceAll("\\s", "");

            if (!this.paramMapping.containsKey(param.getKey())) { continue;}

            Integer index = this.paramMapping.get(param.getKey());

            // 因为页面上传过来的值都是String 类型    但是实际方法中的参数多种多样
            // 这里 对入参进行类型转换
            paramValues[index] = caseStringValue(value, parameterTypes[index]);

        }

        if (this.paramMapping.containsKey(HttpServletRequest.class.getName())) {
            Integer reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if (this.paramMapping.containsKey(HttpServletResponse.class.getName())) {
            Integer respIndex = this.paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }

        // 4. 从handle中取出controller method 然后利用反射机制进行调用
        Object result = handle.getMethod().invoke(handle.getController(), paramValues);
        // 判断返回参数
        if (result == null) {
            return null;
        }
        boolean isModelAndView = handle.getMethod().getReturnType() == ModelAndView.class;
        if (isModelAndView) {
            return (ModelAndView) result;
        }
        return null;
    }

    private Object caseStringValue (String value, Class<?> clazz) {
        if (clazz == String.class) {
            return value;
        } else if (clazz == Integer.class) {
            return Integer.valueOf(value);
        } else if (clazz == int.class) {
            return Integer.valueOf(value).intValue();
        } else {
            return null;
        }
    }


}
