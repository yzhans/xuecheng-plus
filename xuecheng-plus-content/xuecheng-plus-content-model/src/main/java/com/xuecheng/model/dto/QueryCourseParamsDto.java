package com.xuecheng.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author xoo
 * @version 1.0
 * @description TODO
 * @date 2023/3/1 23:00
 */
@Data
@ToString
public class QueryCourseParamsDto {

    //审核状态
    private String auditStatus;

    //课程名称
    private String courseName;

    //发布状态
    private String publishStatus;
}
