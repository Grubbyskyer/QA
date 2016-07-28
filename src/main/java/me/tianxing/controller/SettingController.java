package me.tianxing.controller;

import me.tianxing.service.WendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

/**
 * Created by TX on 2016/7/20.
 */
@Controller
public class SettingController {

    @Autowired
    WendaService wendaService;

    @RequestMapping(path = {"/setting"}, method = {RequestMethod.GET})
    @ResponseBody
    public String setting() {
        return "Setting OK" + wendaService.getMessage(1);
    }

}
