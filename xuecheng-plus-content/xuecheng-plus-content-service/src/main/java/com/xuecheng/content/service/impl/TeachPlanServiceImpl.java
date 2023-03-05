package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.model.dto.SaveTeachplanDto;
import com.xuecheng.model.dto.TeachPlanDto;
import com.xuecheng.model.po.Teachplan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/5 11:32
 */
@Slf4j
@Service
public class TeachPlanServiceImpl implements TeachPlanService {
    @Resource
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeachPlanDto> getTeachPlanTreeNodes(Long courseId) {
        return teachplanMapper.getTeachPlanTreeNodes(courseId);
    }

    @Override
    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto) {
        Long id = saveTeachplanDto.getId();
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan == null) {
            //为空则新增
            teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            int count = getTeachPlanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            teachplan.setOrderby(++count);
            teachplanMapper.insert(teachplan);
        } else {
            //修改
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Override
    public void deleteTeachPlan(Long id) {
        teachplanMapper.deleteById(id);
    }

    //获取orderby
    private int getTeachPlanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getCourseId, courseId);
        wrapper.eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(wrapper);
    }

}
