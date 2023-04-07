package com.xuecheng.content.service;

import com.xuecheng.model.dto.CoursePreviewDto;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author yzhans
 * @version 1.0
 * @description 课程预览、发布接口
 * @date 2023/3/18 2:07
 */
public interface CoursePublishService {

    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @author Mr.M
     * @date 2022/9/16 15:36
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /***
    * @description 提交审核
    * @param companyId 企业id
     * @param courseId 课程id
    * @return void
    * @author yzhans
    * @date 2023/3/21 17:36
    */
    public void commitAudit(Long companyId,Long courseId);

    /***
    * @description 向课程发布表写入数据
    * @param courseId 课程id
    * @param companyId 企业id
    * @return void
    * @author yzhans
    * @date 2023/3/29 6:40
    */
    void coursePublish(Long companyId,Long courseId);
}
