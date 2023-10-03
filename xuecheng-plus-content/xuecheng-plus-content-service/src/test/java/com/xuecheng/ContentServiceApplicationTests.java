package com.xuecheng;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseCategoryService;
import com.xuecheng.model.dto.CourseCategoryDto;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

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

    @Resource
    private CourseCategoryService categoryService;

    @Test
    void testCourseBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(1);
        System.out.println("courseBase = " + courseBase);
    }

    @Test
    void testCourseBaseInfoService() {

        PageResult<CourseBase> list = courseBaseInfoService.queryCourseBaseList(null,new PageParams(), new QueryCourseParamsDto());
        System.out.println(list);
    }

    @Test
    void test2() {
        List<CourseCategoryDto> courseCategoryDtoList = categoryService.queryTreeNodes("1");
        Object json = JSON.toJSON(courseCategoryDtoList);
        System.out.println(json);
    }

    @Test
    void testEnumeration() {
        System.out.println(CommonError.UNKOWN_ERROR.getErrMessage());
        System.out.println(CommonError.UNKOWN_ERROR);
    }
}
