package com.xuecheng;

import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.model.dto.CoursePreviewDto;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author yzhans
 * @version 1.0
 * @description freemarker测试
 * @date 2023/6/11 18:29
 */
@SpringBootTest
public class FreemarkerTest {

    @Resource
    private CoursePublishService coursePublishService;

    @Test
    void testGenerateHtmlByTemplate() throws Exception {

        //配置freemarker
        Configuration configuration = new Configuration(Configuration.getVersion());

        //选指定模板路径,classpath下templates下
        //得到classpath路径
        String classpath = Objects.requireNonNull(this.getClass().getResource("/templates/")).toString().replaceAll("file:/","");
        configuration.setDefaultEncoding("utf-8");

        configuration.setDirectoryForTemplateLoading(new File(classpath));
        //设置字符编码

        //指定模板文件名称
        Template template = configuration.getTemplate("course_template.ftl");

        //查询课程信息作为模板信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(121L);

        HashMap<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewInfo);

        //静态化
        //参数1：模板，参数2：数据模型
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        //将静态化内容输出到文件中
        InputStream inputStream = IOUtils.toInputStream(content);
        //输出流
        FileOutputStream outputStream = new FileOutputStream("D:\\download\\xczx\\test.html");
        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }

    @Test
    void name() throws IOException {
        String classpath = Objects.requireNonNull(this.getClass().getResource("/templates/")).toString();
        System.out.println(classpath);
    }
}
