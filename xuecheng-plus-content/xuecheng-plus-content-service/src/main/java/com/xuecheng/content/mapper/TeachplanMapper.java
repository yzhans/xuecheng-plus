package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.model.dto.TeachPlanDto;
import com.xuecheng.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author yzhans
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    List<TeachPlanDto> getTeachPlanTreeNodes(Long courseId);

}
