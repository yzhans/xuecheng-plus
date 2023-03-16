package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChangException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.model.po.CourseTeacher;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/6 16:27
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Resource
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public CourseTeacher createCourseTeacher(CourseTeacher c) {
        if (c == null)
            XueChangException.cast("信息为空");
        Long id = c.getId();
        if (id == null) {
            //空则表示不存在该教师信息 进行添加
            c.setCreateDate(LocalDateTime.now());
            int insert = courseTeacherMapper.insert(c);
            if (insert <= 0)
                XueChangException.cast("添加失败");
            return c;
        } else {
            //修改
            CourseTeacher courseTeacher = courseTeacherMapper.selectById(id);
            BeanUtils.copyProperties(c, courseTeacher);
            int i = courseTeacherMapper.updateById(courseTeacher);
            if (i <= 0)
                XueChangException.cast("修改失败");
            return courseTeacher;
        }
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        if (courseId == null || teacherId == null)
            XueChangException.cast("参数无效");
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getId, teacherId).
                eq(CourseTeacher::getCourseId,courseId);
        int i = courseTeacherMapper.delete(wrapper);
        if (i <= 0)
            XueChangException.cast("删除失败");
    }

    @Override
    public List<CourseTeacher> getTeacherInformation(Long id) {
        // SELECT * FROM course_teacher WHERE course_id = 117
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, id);
        return courseTeacherMapper.selectList(queryWrapper);
    }
}
