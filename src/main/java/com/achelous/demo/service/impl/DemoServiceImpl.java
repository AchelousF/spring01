package com.achelous.demo.service.impl;

import com.achelous.demo.service.IDemoService;
import com.achelous.spring.annotation.Service;
import com.achelous.spring.annotation.Transactional;

/**
 * @Auther: fanJiang
 * @Date: Create in 11:03 2018/4/22
 */
@Service
public class DemoServiceImpl implements IDemoService {


    @Transactional
    public String get(String name) {
        return "my name is " + name;
    }
}
