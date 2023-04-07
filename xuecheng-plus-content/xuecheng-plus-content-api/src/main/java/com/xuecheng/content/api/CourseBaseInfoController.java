package com.xuecheng.content.api;



import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.model.dto.AddCourseDto;
import com.xuecheng.model.dto.CourseBaseInfoDto;
import com.xuecheng.model.dto.EditCourseDto;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * @author xoo
 * @version 1.0
 * @description TODO
 * @date 2023/3/1 23:19
 */
@Api(value = "课程管理接口", tags = "课程管理接口")
@RestController
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/list")
    public PageResult<CourseBase> searchData(
            PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseInfoService.queryCourseBaseList(params, queryCourseParamsDto);
    }

    @ApiOperation("新增课程")
    @PostMapping()  //添加校验组
    public CourseBaseInfoDto createCourseBase(
            @RequestBody @Validated AddCourseDto addCourseDto) {
        //企业id传入
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("课程编辑回显接口")
    @GetMapping("/{id}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable("id") Long id) {
        return courseBaseInfoService.courseBaseInfoService(id);
    }

    @ApiOperation("课程修改接口")
    @PutMapping()
    public CourseBaseInfoDto updateCourseBaseById(@RequestBody EditCourseDto editCourseDto) {
        return courseBaseInfoService.updateCourseBaseById(1232141425L,editCourseDto);
    }


    @ApiOperation("课程删除接口")
    @DeleteMapping("/{courseId}")
    public void deleteCourseBaseById(@PathVariable("courseId") Long courseId) {
        courseBaseInfoService.deleteCourseBaseById(1232141425L,courseId);
    }

}
