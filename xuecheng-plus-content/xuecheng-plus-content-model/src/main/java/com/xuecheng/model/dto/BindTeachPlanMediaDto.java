package com.xuecheng.model.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/17 1:45
 */
@Api(value = "BindTeachplanMediaDto", description = "教学计划-媒资绑定提交数据")
@Data
public class BindTeachPlanMediaDto {

    @ApiModelProperty(value = "媒资文件id", required = true)
    private String mediaId;

    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String fileName;

    @ApiModelProperty(value = "课程计划标识", required = true)
    private Long teachplanId;
}
