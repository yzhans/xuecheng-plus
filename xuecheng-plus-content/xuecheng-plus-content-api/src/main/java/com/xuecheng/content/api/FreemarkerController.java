package com.xuecheng.content.api;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author yzhans
 * @version 1.0
 * @description Freemarker入门程序
 * @date 2023/3/17 5:19
 */
@Api(value = "Freemarker接口", tags = "Freemarker接口")
@RestController
@Slf4j
public class FreemarkerController {

    @GetMapping("/testfreemarker")
    public ModelAndView test() {
        ModelAndView modelAndView = new ModelAndView();
        //指定模型
        modelAndView.addObject("name", "yzhans");
        //指定模板
        modelAndView.setViewName("test");//根据视图名称加.ftl找到模板
        return modelAndView;
    }

}
