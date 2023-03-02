package com.xuecheng;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/2 11:16
 */
@SpringBootTest
public class ContentServiceApplicationTests {

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @Test
    void testCourseBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(1);
        System.out.println("courseBase = " + courseBase);
    }

    @Test
    void testCourseBaseInfoService() {

        PageResult<CourseBase> list = courseBaseInfoService.queryCourseBaseList(new PageParams(), new QueryCourseParamsDto());
        System.out.println(list);
    }
}
