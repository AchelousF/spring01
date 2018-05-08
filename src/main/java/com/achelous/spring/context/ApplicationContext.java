package com.achelous.spring.context;

import com.achelous.spring.annotation.Autowire;
import com.achelous.spring.annotation.Controller;
import com.achelous.spring.annotation.Service;
import com.achelous.spring.aop.AopConfig;
import com.achelous.spring.beans.BeanDefinition;
import com.achelous.spring.beans.BeanPostProcessor;
import com.achelous.spring.beans.BeanWrapper;
import com.achelous.spring.context.support.BeanDefinitionReader;
import com.achelous.spring.core.BeanFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: fanJiang
 * @Date: Create in 20:20 2018/4/24
 */
public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory{

    private String [] configLocations;

    private BeanDefinitionReader reader;


    // 用来保证  注册式单例的容器
    private final Map<String, Object> beanCacheMap = new ConcurrentHashMap<>();

    // 用来存储所有被代理的对象
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();


    public ApplicationContext(String... locations) {

        this.configLocations = locations;

        refresh();

    }

    /**
     * 容器初始化方法
     */
    public void refresh(){

        // 1. 定位
        this.reader = new BeanDefinitionReader(configLocations);

        // 2. 加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();


        // 3. 注册
        doRegistry(beanDefinitions);


        // 4. 依赖注入 (lazy-init == false)
        // 相当于自动调用getBean()
        doAutowired();


    }

    private void doAutowired() {
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {

            String beanName = beanDefinitionEntry.getKey();

            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                // 判断该对象是否配置懒加载  如果是立即加载调用getBean方法   (还应处理单例及原型模式，这里默认使用了单例)
                getBean(beanName);
            }

        }
    }

    /**
     * 依赖注入  具体实现
     * @param beanName
     * @param instance
     */
    public void populateBean(String beanName, Object instance) {

        Class<?> clazz = instance.getClass();

        if (!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class))) {
            return ;
        }

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(Autowire.class)) { continue; }

            Autowire autowire = field.getAnnotation(Autowire.class);

            String autowireBeanName = autowire.value().trim();

            if ("".equals(autowireBeanName)) {
                autowireBeanName = field.getType().getSimpleName();
            }

            field.setAccessible(true);

            try {
                // 在这里调用自动注入可能会有问题   因为被依赖对象可能还未实例化
                // 判空   如果被依赖对象还未被实例化   递归调用getBean方法对被依赖对象进行实例化
                if (null == this.beanWrapperMap.get(autowireBeanName)) {
                    getBean(autowireBeanName);
                }
                field.set(instance, this.beanWrapperMap.get(autowireBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


        }


    }


    // 将解析好的beanDefinition 放入Spring 容器中
    private void doRegistry(List<String> beanDefinitions) {


        try {
            //  beanName 三种情况
            // 1. 默认类名首字母小写
            // 2. 用户自定义名称
            // 3. 接口注入

            for (String className : beanDefinitions) {

                Class<?> beanClass = Class.forName(className);

                // 接口不能实例化   用其实现类进行实例化
                if (beanClass.isInterface()) { continue; }

                BeanDefinition beanDefinition = reader.registryBeanDefinition(className);

                if (beanDefinition != null) {
                    // 将加载完成的beanDefinition对象 注册到ioc容器中
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
                }

                Class<?>[] interfaces = beanClass.getInterfaces();
                // 对接口类型的注入处理    使用接口名作为key 实现类作为value
                for (Class<?> i : interfaces) {
                    // 如果有多个实现类  只能采用别名方式
                    // 否则spring中会抛出异常
                    this.beanDefinitionMap.put(i.getSimpleName(), beanDefinition);
                }

                // 至此 完成容器的初始化

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 通过读取beanDefinition中的信息  通过反射机制创建实例并返回
     * Spring 在此方法不会返回最原始对象   会使用beanWrapper进行包装返回
     * 包装器模式:
     * 1. 保留原来的OOP关系
     * 2. 需要对其进行扩展，增强
     * @param beanName
     * @return
     */
    @Override
    public Object getBean(String beanName) {

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        try {
            BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
            Object instance = instantiateBean(beanDefinition);
            if (null == instance) { return null; }

            // 在实例初始化之前的  处理器
            beanPostProcessor.postPrecessBeforeInitialization(instance, beanName);

            BeanWrapper beanWrapper = new BeanWrapper(instance);

            beanWrapper.setAopConfig(instantiateAopConfig(beanDefinition));

            beanWrapper.setPostProcessor(beanPostProcessor);
            this.beanWrapperMap.put(beanName, beanWrapper);

            // 在实例初始化之后的  处理器
            beanPostProcessor.postPrecessAfterInitialization(instance, beanName);

            populateBean(beanName, beanWrapper.getOriginalInstance());

            return this.beanWrapperMap.get(beanName).getWrapperInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private AopConfig instantiateAopConfig(BeanDefinition beanDefinition) throws Exception {
        AopConfig config = new AopConfig();

        // 解析切面表达式    (获取需要增强的方法)
        String expression = reader.getConfig().getProperty("pointCut");
        // 解析before 和 after 方法    增强逻辑
        String [] before = reader.getConfig().getProperty("aspectBefore").split("\\s");
        String [] after = reader.getConfig().getProperty("aspectAfter").split("\\s");

        String className = beanDefinition.getBeanClassName();

        // 获取beanDefinition
        Class<?> clazz = Class.forName(className);

        // 切面表达式正则
        Pattern pattern = Pattern.compile(expression);

        //  这里因为before 方法和after方法在同一个类中   仅解析before[0]
        Class<?> aspectClass = Class.forName(before[0]);

        for (Method method: clazz.getMethods()) {
            // 判断方法是否匹配切面表达式
            Matcher matcher = pattern.matcher(method.toString());
            if (matcher.matches()) {
                // 如果匹配说明该方法需要 aop增强  将增加方法增加到aopConfig中。
                config.put(method, aspectClass.newInstance(), new Method[]{aspectClass.getMethod(before[1]),aspectClass.getMethod(after[1])});
            }
        }

        return config;
    }


    // 通过beanDefinition 对bean 实例化
    private Object instantiateBean(BeanDefinition beanDefinition) {

        Object instance;
        String className = beanDefinition.getBeanClassName();

        try {
            synchronized (this.beanCacheMap) {
                // 获取bean  在缓存中获取
                if (this.beanCacheMap.containsKey(className)) {
                    instance = this.beanCacheMap.get(className);
                } else {
                    // 如果缓存没有    实例化bean并添加到缓存中
                    Class<?> cla = Class.forName(className);
                    instance = cla.newInstance();
                    this.beanCacheMap.put(className, instance);

                }
            }

            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public String [] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }


    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }

}
