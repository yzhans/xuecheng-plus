package com.xuecheng.content.api;

import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.model.po.CourseTeacher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/6 16:26
 */
@Api(value = "教师信息接口", tags = "教师信息接口")
@RestController
@Slf4j
@RequestMapping("/courseTeacher")
public class CourseTeacherController {

    @Resource
    private CourseTeacherService courseTeacherService;

    @ApiOperation("教师信息查询")
    @GetMapping("/list/{id}")
    public List<CourseTeacher> getTeacherInformation(@PathVariable("id") Long id) {
        return courseTeacherService.getTeacherInformation(id);
    }

    @ApiOperation("教师添加或修改接口")
    @PostMapping()
    public CourseTeacher createCourseTeacher(@RequestBody CourseTeacher c) {
        return courseTeacherService.createCourseTeacher(c);
    }

    @ApiOperation("教师删除接口")
    @DeleteMapping("/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(
            @PathVariable("courseId") Long courseId,
            @PathVariable("teacherId") Long teacherId) {
        courseTeacherService.deleteCourseTeacher(courseId,teacherId);
    }
}
