package com.xuecheng.content.api;

import com.xuecheng.content.service.CourseCategoryService;
import com.xuecheng.model.dto.CourseCategoryDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/2 23:39
 */
@Api(value = "课程分类接口",tags = "课程分类接口")
@RestController
public class CourseCategoryController {

    @Resource
    private CourseCategoryService categoryService;

    @ApiOperation("课程分类查询")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryDto> queryTreeNodes() {
        return categoryService.queryTreeNodes("1");
    }
}
