package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;

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
    * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
    * @author yzhans
    * @date 2023/3/2 13:42
    */
    PageResult<CourseBase> queryCourseBaseList(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto);

}
