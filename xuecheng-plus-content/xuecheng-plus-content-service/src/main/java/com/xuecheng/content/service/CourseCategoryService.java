package com.xuecheng.content.service;

import com.xuecheng.model.dto.CourseCategoryDto;

import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/3 0:11
 */
public interface CourseCategoryService {

    /***
    * @description 课程分类查询
    * @param id
    * @return java.util.List<com.xuecheng.model.dto.CourseCategoryDto>
    * @author yzhans
    * @date 2023/3/3 0:13
    */
    List<CourseCategoryDto> queryTreeNodes(String id);

}
