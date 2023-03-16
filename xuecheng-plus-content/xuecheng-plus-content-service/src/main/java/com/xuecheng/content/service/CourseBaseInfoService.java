package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;

import com.xuecheng.model.dto.AddCourseDto;
import com.xuecheng.model.dto.CourseBaseInfoDto;
import com.xuecheng.model.dto.EditCourseDto;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/2 13:41
 */
public interface CourseBaseInfoService {

    /***
    * @description 课程查询
    * @param params 分页参数
     * @param queryCourseParamsDto  查询条件
    * @return 分页查询后的列表
    * @author yzhans
    * @date 2023/3/2 13:42
    */
    PageResult<CourseBase> queryCourseBaseList(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto);

    /***
    * @description 新增课程
    * @param companyId 培训机构id
     * @param addCourseDto 新增课程信息
    * @return 课程信息包括基本信息 营销信息
    * @author yzhans
    * @date 2023/3/3 15:01
    */
    CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto);

    /***
    * @description 按id查询课程基本数据
    * @param id 课程id
    * @return 课程基本数据
    * @author yzhans
    * @date 2023/3/5 0:22
    */
    CourseBaseInfoDto courseBaseInfoService(Long id);

    /***
    * @description 修改课程基本信息
    * @param editCourseDto 修改的课程数据
    * @return 课程基本数据
    * @author yzhans
    * @date 2023/3/5 2:03
    */
    CourseBaseInfoDto updateCourseBaseById(Long companyId,EditCourseDto editCourseDto);

    /***
    * @description 课程删除
    * @param companyId 企业id
     * @param courseId 课程id
    * @return void
    * @author yzhans
    * @date 2023/3/7 13:47
    */
    void deleteCourseBaseById(Long companyId, Long courseId);
}
