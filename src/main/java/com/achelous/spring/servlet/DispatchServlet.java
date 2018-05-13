package com.achelous.spring.servlet;

import com.achelous.spring.annotation.Controller;
import com.achelous.spring.annotation.RequestMapping;
import com.achelous.spring.annotation.RequestParam;
import com.achelous.spring.aop.AopProxyUtils;
import com.achelous.spring.context.ApplicationContext;
import com.achelous.spring.webmvc.HandlerAdapter;
import com.achelous.spring.webmvc.HandlerMapping;
import com.achelous.spring.webmvc.ModelAndView;
import com.achelous.spring.webmvc.ViewResolve;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @Auther: fanJiang
 * @Date: Create in 10:52 2018/4/22
 */
public class DispatchServlet extends HttpServlet {

    private final String LOCATION = "contextConfigLocation";

    // TODO 这样设计的好处
    private List<HandlerMapping> handlerMappings = new ArrayList<>();

    private Map< HandlerMapping ,HandlerAdapter> handlerAdapters = new HashMap<>();

    private List<ViewResolve> viewResolves = new ArrayList<>();


    @Override
    public void init(ServletConfig config) throws ServletException {

        // 相当于初始化ioc 容器
        ApplicationContext context = new ApplicationContext(config.getInitParameter(LOCATION));

        // 初始化九大组件
        initStrategies(context);
    }

    private void initStrategies(ApplicationContext context) {

        initMultipartResolver(context);
        initLocaleResolver(context);
        initThemeResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
        initFlashMapManager(context);
    }

    private void initFlashMapManager(ApplicationContext context) {
        // TODO
    }

    private void initThemeResolver(ApplicationContext context) {
        // TODO
    }

    private void initLocaleResolver(ApplicationContext context) {
        // TODO
    }

    private void initMultipartResolver(ApplicationContext context) {
        // TODO
    }

    private void initRequestToViewNameTranslator(ApplicationContext context) {
        // TODO
    }

    private void initHandlerExceptionResolvers(ApplicationContext context) {
        // TODO
    }


    /**
     * 实现动态模板的解析
     * @param context
     */
    private void initViewResolvers(ApplicationContext context) {
        // 在这里解决页面名字于模板文件关联的问题   jsp、html

        String templateRoot = context.getConfig().getProperty("templateRoot");

        String filePath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateDir = new File(filePath);

        for (File templateFile: templateDir.listFiles()) {
            this.viewResolves.add(new ViewResolve(templateFile.getName(), templateFile));
        }

    }


    /**
     * 用来动态匹配method参数，包括类型转换，动态赋值
     * @param context
     */
    private void initHandlerAdapters(ApplicationContext context) {
        // 在初始化阶段     将参数的 名字或类型按一定的顺序保存
        // 之后用反射调用的时候 传入的形参是一个数组
        // 通过记录参数的位置index   挨个从数组中添值

        for (HandlerMapping handlerMapping: this.handlerMappings) {

            // 每一个方法都有一个参数列表  这里保留的形参列表
            Map<String , Integer> paramMapping = new HashMap<>();

            Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();

            // 这里只是处理命名参数
            for (int i =0;i < pa.length;i ++) {
                for (Annotation annotation: pa[i]) {
                    if (annotation instanceof RequestParam) {
                        String paramName = ((RequestParam) annotation).value();

                        if (!"".equals(paramName.trim())) {
                            // 将参数的名字及 参数位置index保存
                            paramMapping.put(paramName, i);
                        }

                    }
                }
            }

            // 这里处理非命名参数
            // 这里只处理request 及 response;

            Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramMapping.put(type.getName(), i);
                }
            }

            // 将处理好的参数列表 及 对应的handlerMapping 保存到handleAdapter中
            this.handlerAdapters.put(handlerMapping, new HandlerAdapter(paramMapping));
        }


    }

    /**
     * 用来保存  controller中requestMapping配置的URL和method的对应关联
     * @param context
     */
    private void initHandlerMappings(ApplicationContext context) {

        // 首先从容器中取到所有的实例名称
        String[] beanNames = context.getBeanDefinitionNames();

        for (String beanName: beanNames) {
            // 这里因为增加了 aop代理  获取到的是代理对象(proxy$xx) 读取不到注解
            Object proxyController = context.getBean(beanName);
            Object controller;
            try {
                controller = AopProxyUtils.getTargetObject(proxyController);
            } catch (Exception e) {
                e.printStackTrace();
                return ;
            }
            Class<?> clazz = controller.getClass();
            // 判断是否具有制定注解
            if (!clazz.isAnnotationPresent(Controller.class)) {continue;}

            String baseUrl = "";

            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl = requestMapping.value();
            }

            // 获取controller 下所有的方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {continue;}
                // 如果方法标注了 requestMapping 注解   将该方法注册到handleMappings 中
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);

                this.handlerMappings.add(new HandlerMapping(pattern, controller, method));

            }

        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        HandlerMapping handle = getHandler(req);

        // 未找到匹配项    返回404 异常
        if (handle == null) {
            resp.getWriter().write("404 Not Found!\r\n");
            return;
        }

        HandlerAdapter adapter = getHandlerAdapter(handle);

        // 未找到匹配项   参数解析异常
        if (adapter == null) {
            resp.getWriter().write("405 bed request!\r\n");
            return;
        }
        // 调用请求url对用的方法   得到返回值
        ModelAndView mv = adapter.handle(req, resp, handle);

        processDispatchResult(resp, mv);
    }

    private void processDispatchResult(HttpServletResponse resp, ModelAndView mv) throws Exception {

        if (null == mv) {return;}

        if (this.viewResolves.isEmpty()) {return;}

        for (ViewResolve viewResolve : this.viewResolves) {

            if (!mv.getViewName().equals(viewResolve.getViewName())) {
                continue;
            }
            String out = viewResolve.viewResolve(mv);

            if (out != null) {
                resp.getWriter().write(out);
                break;
            }
        }

    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handle) {
        if (this.handlerAdapters.isEmpty()) {return null;}
        return this.handlerAdapters.get(handle);
    }

    private HandlerMapping getHandler(HttpServletRequest req) {

        if (this.handlerMappings.isEmpty()) {return null;}

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();

        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (HandlerMapping handler : this.handlerMappings) {
            // 遍历注册的handlerMapping 列表 并判断当前请求的url 是否与handler匹配 找到匹配项并返回
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) {continue;}
            return handler;
        }


        return null;
    }


    private String firstLowerCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
