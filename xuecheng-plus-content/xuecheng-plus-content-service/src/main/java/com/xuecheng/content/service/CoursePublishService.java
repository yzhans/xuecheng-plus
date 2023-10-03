package com.xuecheng.content.service;

import com.xuecheng.model.dto.CoursePreviewDto;
import com.xuecheng.model.po.CoursePublish;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;

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

    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     * @author yzhans
     * @date 2023/3/29 6:40
     */
    public File generateCourseHtml(Long courseId);
    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     * @return void
     * @author yzhans
     * @date 2023/3/29 6:40
     */
    public void  uploadCourseHtml(Long courseId, File file);

    /***
     * @description 查询课程发布
     * @param courseId 课程id
     * @return com.xuecheng.model.po.CoursePublish
     * @author xoo
     * @date 2023/10/3 5:23
     */
    public CoursePublish getCoursePublish(Long courseId);

}
