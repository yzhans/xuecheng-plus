package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import com.xuecheng.model.dto.AddCourseDto;
import com.xuecheng.model.dto.CourseBaseInfoDto;
import com.xuecheng.model.dto.EditCourseDto;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * @author xoo
 * @version 1.0
 * @description 课程管理接口
 * @date 2023/3/1 23:19
 */
@Api(value = "课程管理接口", tags = "课程管理接口")
@RestController
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")//指定权限标识符，拥有此权限才可以访问此方法
    @PostMapping("/list")
    public PageResult<CourseBase> searchData(
            PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        //获取当前用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        //获取企业id
        String companyIds = user.getCompanyId();
        if (StringUtils.isNotEmpty(companyIds)) {
            companyId = Long.parseLong(companyIds);
        }
        return courseBaseInfoService.queryCourseBaseList(companyId,params, queryCourseParamsDto);
    }

    @ApiOperation("新增课程")
    @PostMapping()  //添加校验组
    public CourseBaseInfoDto createCourseBase(
            @RequestBody @Validated AddCourseDto addCourseDto) {
        //企业id传入
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("查询指定课程信息接口")
    @GetMapping("/{id}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable("id") Long id) {
        //取出当前用户身份
        //Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //System.out.println(principal);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user);
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
