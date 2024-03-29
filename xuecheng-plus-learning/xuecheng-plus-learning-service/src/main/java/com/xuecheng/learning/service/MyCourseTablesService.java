package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

/**
 * @author xoo
 * @version 1.0
 * @description 选课相关接口
 * @date 2023/10/3 7:57
 */
public interface MyCourseTablesService {

    /***
     * @description 添加选课
     * @param userId 用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcChooseCourseDto
    * @author xoo
    * @date 2023/10/3 7:58
    */
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * @description 判断学习资格
     * @param userId 用户Id
     * @param courseId 课程Id
     * @return XcCourseTablesDto 学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     * @author xoo
     * @date 2023/10/3 14:43
     */
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /***
    * @description 更新保存选课状态
    * @param chooseCourseId 选择课程id
    * @return boolean
    * @author xoo
    * @date 2023/10/22 5:51
    */
    public boolean saveChooseCourseSuccess(String chooseCourseId);
}
