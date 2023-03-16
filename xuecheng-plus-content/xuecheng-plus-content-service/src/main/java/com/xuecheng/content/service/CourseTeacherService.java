package com.xuecheng.content.service;

import com.xuecheng.model.dto.SaveTeachplanDto;
import com.xuecheng.model.dto.TeachPlanDto;
import com.xuecheng.model.po.CourseTeacher;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/5 11:31
 */
public interface CourseTeacherService {

    /***
    * @description 查询该课程下的所有教师信息
    * @param id 该课程的id
    * @return 教师信息的集合
    * @author yzhans
    * @date 2023/3/6 23:37
    */
    List<CourseTeacher> getTeacherInformation(Long id);

    /***
    * @description 修改或新增教师
    * @param c 教师信息
    * @return 教师信息
    * @author yzhans
    * @date 2023/3/6 23:53
    */
    CourseTeacher createCourseTeacher(CourseTeacher c);

    void deleteCourseTeacher(Long courseId, Long teacherId);
}
