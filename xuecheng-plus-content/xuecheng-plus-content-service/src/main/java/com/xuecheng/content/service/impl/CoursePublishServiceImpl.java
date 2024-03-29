package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChangException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.model.dto.CourseBaseInfoDto;
import com.xuecheng.model.dto.CoursePreviewDto;
import com.xuecheng.model.dto.TeachPlanDto;
import com.xuecheng.model.po.CourseBase;
import com.xuecheng.model.po.CourseMarket;
import com.xuecheng.model.po.CoursePublish;
import com.xuecheng.model.po.CoursePublishPre;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/18 2:07
 */
@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private TeachPlanService teachPlanService;

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    private CoursePublishMapper coursePublishMapper;

    @Resource
    private MqMessageService mqMessageService;

    @Resource
    private MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //课程基本信息 营销信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.courseBaseInfoService(courseId);
        //课程计划信息
        List<TeachPlanDto> teachPlanTreeNodes = teachPlanService.getTeachPlanTreeNodes(courseId);
        //保存返回
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachPlanTreeNodes);
        return coursePreviewDto;
    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        //如果课程的审核状态为已提交则不允许提交
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.courseBaseInfoService(courseId);
        if (courseBaseInfoDto == null) {
            XueChangException.cast("课程找不到");
        }
        //判断是否审核
        String auditStatus = courseBaseInfoDto.getAuditStatus();
        if ("202003".equals(auditStatus)) {
            XueChangException.cast("课程已提交请等待审核");
        }
        // 课程的图片、计划信息没有填写也不允许提交
        String pic = courseBaseInfoDto.getPic();
        if (StringUtils.isEmpty(pic)) {
            XueChangException.cast("请上传课程图片");
        }
        //查询课程计划
        List<TeachPlanDto> teachPlanTreeNodes = teachPlanService.getTeachPlanTreeNodes(courseId);
        if (teachPlanTreeNodes == null || teachPlanTreeNodes.size() == 0) {
            XueChangException.cast("请编写课程计划");
        }
        //查询到课程基本信息、营销信息、计划等信息插入到课程预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfoDto, coursePublishPre);
        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        String teachPlanTreeNodesJson = JSON.toJSONString(teachPlanTreeNodes);
        coursePublishPre.setMarket(courseMarketJson);
        coursePublishPre.setTeachplan(teachPlanTreeNodesJson);
        //设置状态已提交
        coursePublishPre.setStatus("202003");
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //设置企业id
        coursePublishPre.setCompanyId(companyId);
        //查询预发布表,如果有记录则更新,没有则插入
        CoursePublishPre preReleaseTable = coursePublishPreMapper.selectById(courseId);
        if (preReleaseTable == null) {
            //插入
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            //更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void coursePublish(Long companyId, Long courseId) {
        //查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChangException.cast("课程没有审核记录，无法发布");
        }
        if (!companyId.equals(coursePublishPre.getCompanyId())) {
            XueChangException.cast("请登录该课程绑定的公司账号进行发布");
        }
        //取出状态 进行校验
        String status = coursePublishPre.getStatus();
        if (!"202004".equals(status)) {
            XueChangException.cast("课程没有审核通过不允许发布");
        }
        //向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        CoursePublish publish = coursePublishMapper.selectById(courseId);
        if (publish == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
        //向消息表写入数据
        saveCoursePublishMessage(courseId);
        //将预发布表数据删除
        coursePublishPreMapper.deleteById(coursePublishPre);
    }

    /***
     * @description 向定时任务消息表写入数据
     * @param courseId 课程id
     * @return void
     * @author yzhans
     * @date 2023/3/31 22:49
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage coursePublish = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (courseId == null) {
            XueChangException.cast(CommonError.UNKOWN_ERROR);
        }
    }


    @Override
    public File generateCourseHtml(Long courseId){

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        File htmlFile = null;
        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = Objects.requireNonNull(this.getClass().getResource("/templates/")).toString().replaceAll("file:/", "");
            configuration.setDefaultEncoding("utf-8");

            configuration.setDirectoryForTemplateLoading(new File(classpath));
            //设置字符编码

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //查询课程信息作为模板信息
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

            //将静态化内容输出到文件中
            inputStream = IOUtils.toInputStream(content);
            //临时文件
            htmlFile = File.createTempFile("temporaryFileForCourseRelease",".html");
            //输出流
            outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);

        } catch (Exception e) {
            log.debug("生成课程Html失败：{}，课程id：{}", e, courseId);
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream); // 在finally语句块中关闭输入流
            IOUtils.closeQuietly(outputStream); // 在finally语句块中关闭输出流
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        try {
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
            String s = mediaServiceClient.uploadFile(multipartFile, "course/" + courseId + ".html");
            if (StringUtils.isEmpty(s)) {
                log.debug("上传静态文件异常，课程id为：{}", courseId);
                XueChangException.cast("上传静态文件异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            XueChangException.cast("上传静态文件异常");
        }
    }

    /***
    * @description 查询课程发布
    * @param courseId 课程id
    * @return com.xuecheng.model.po.CoursePublish
    * @author xoo
    * @date 2023/10/3 5:23
    */
    public CoursePublish getCoursePublish(Long courseId){
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish ;
    }
}
