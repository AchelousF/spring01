package com.achelous.spring.webmvc;

import java.util.Map;

/**
 * @Auther: fanJiang
 * @Date: Create in 15:16 2018/4/29
 */
public class ModelAndView {

    private String viewName;
    private Map<String, Object> model;

    public ModelAndView(String viewName, Map<String, Object> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }
}
