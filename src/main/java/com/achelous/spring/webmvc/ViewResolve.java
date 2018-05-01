package com.achelous.spring.webmvc;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: fanJiang
 * @Date: Create in 15:19 2018/4/29
 */

// 实现将一个静态文件变为动态页面
 // 最终返回字符串   由response 输出
public class ViewResolve {

    private String viewName;

    private File templateFile;


    public ViewResolve(String viewName, File templateFile) {
        this.viewName = viewName;
        this.templateFile = templateFile;
    }

    public String viewResolve(ModelAndView mv) throws Exception {
        StringBuffer sb = new StringBuffer();

        RandomAccessFile ra = new RandomAccessFile(this.templateFile, "r");

        String line = null;
        while (null != (line = ra.readLine())) {
            Matcher m = matcher(line);
            while (m.find()) {
                for (int i = 0; i <= m.groupCount(); i++) {
                    // 获取 ${} 中间的字符串
                    String param = m.group(i);
                    Object paramValue = mv.getModel().get(param);
                    if (null == paramValue) { continue;}
                    line = line.replaceAll("$\\{" + param +"\\}", paramValue.toString());
                }
            }
            sb.append(line);
        }
        return sb.toString();
    }

    private Matcher matcher(String str) {
        Pattern pattern = Pattern.compile("$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(str);
    }


    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }
}
