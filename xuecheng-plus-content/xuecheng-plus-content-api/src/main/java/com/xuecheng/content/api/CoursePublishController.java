package com.xuecheng.content.api;

import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.model.dto.CoursePreviewDto;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

/**
 * @author yzhans
 * @version 1.0
 * @description 课程发布接口
 * @date 2023/3/17 23:19
 */
@Slf4j
@Controller
public class CoursePublishController {

    @Resource
    private CoursePublishService coursePublishService;

    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {
        ModelAndView modelAndView = new ModelAndView();
        //查询课程信息作为模板信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        //指定模型
        modelAndView.addObject("model", coursePreviewInfo);
        //指定模板
        modelAndView.setViewName("course_template");//根据视图名称加.ftl找到模板
        return modelAndView;
    }

    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        coursePublishService.commitAudit(1232141425L, courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursePublish(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.coursePublish(companyId,courseId);
    }

}
