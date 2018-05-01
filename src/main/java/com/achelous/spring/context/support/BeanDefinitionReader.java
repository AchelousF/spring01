package com.achelous.spring.context.support;

import com.achelous.spring.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Auther: fanJiang
 * @Date: Create in 20:27 2018/4/24
 */

/***
 * 对配置文件进行查找、解析、读取。
 */
public class BeanDefinitionReader {

    private Properties config = new Properties();

    private List<String> registryBeanClasses = new ArrayList<>();



    private final String SCAN_PACKAGE = "scanPackage";



    public BeanDefinitionReader(String... locations) {
        // TODO 可配置多个配置文件    这里只使用了第一个
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));

        try {
            // 将servlet中配置的文件路径 读取到context上下文中
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));

    }


    public List<String> loadBeanDefinitions() {
        return this.registryBeanClasses;
    }

    /**
     * 解析并注册beanDefinition  每注册一个className 返回一个BeanDefinition
     * @param className
     * @return
     */
    public BeanDefinition registryBeanDefinition(String className){

        if (this.registryBeanClasses.contains(className)) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            // bean在ioc容器中的名字
            beanDefinition.setFactoryBeanName(firstLowerCase(className.substring(className.lastIndexOf(".") + 1)));
            return beanDefinition;
        }


        return null;
    }


    public Properties getConfig() {
        return this.config;
    }


    // 递归扫描所有相关联的class  并保存到一个List中
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));

        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                // 如果查找到目录文件    递归调用扫描方法 扫描下一层级文件
                doScanner(scanPackage + "." + file.getName());
            } else {
                registryBeanClasses.add(scanPackage + "." +file.getName().replace(".class", ""));
            }
        }

    }

    private String firstLowerCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
