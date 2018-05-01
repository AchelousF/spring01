package com.achelous.demo.controller;


import com.achelous.spring.annotation.Controller;
import com.achelous.spring.annotation.RequestMapping;
import com.achelous.spring.webmvc.ModelAndView;

/**
 * @Auther: fanJiang
 * @Date: Create in 11:02 2018/4/22
 */
@Controller
@RequestMapping("myAction")
public class MyAction {

    @RequestMapping("index.html")
    public ModelAndView index(){
        return null;
    }
}
