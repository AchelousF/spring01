package com.achelous.demo.controller;



import com.achelous.demo.service.IDemoService;
import com.achelous.spring.annotation.Autowire;
import com.achelous.spring.annotation.Controller;
import com.achelous.spring.annotation.RequestMapping;
import com.achelous.spring.annotation.RequestParam;
import com.achelous.spring.webmvc.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Auther: fanJiang
 * @Date: Create in 10:59 2018/4/22
 */
@Controller
@RequestMapping("/demo")
public class DemoAction {

    @Autowire
    IDemoService demoService;

    @RequestMapping("/query")
    public ModelAndView query(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam("name") String name) {
        String s = demoService.get(name);


        System.out.println(s);
        return null;
    }

}
