package com.xuecheng.model.dto;

import com.xuecheng.model.po.CourseTeacher;
import com.xuecheng.model.po.Teachplan;
import com.xuecheng.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description 关联表
 * @date 2023/3/5 10:22
 */
@Data
public class TeachPlanDto extends Teachplan {

    //关联的媒体资讯信息
    TeachplanMedia teachplanMedia;

    //子目录
    List<TeachPlanDto> teachPlanTreeNodes;

}
