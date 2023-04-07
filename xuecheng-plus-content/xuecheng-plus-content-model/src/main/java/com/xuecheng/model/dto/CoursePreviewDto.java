package com.xuecheng.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description 课程预览数据模型
 * @date 2023/3/18 2:03
 */
@Data
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;

    //课程计划信息
    List<TeachPlanDto> teachplans;

    //师资信息暂时不加...

}
