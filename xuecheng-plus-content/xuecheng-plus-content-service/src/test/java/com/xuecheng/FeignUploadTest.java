package com.xuecheng;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author yzhans
 * @version 1.0
 * @description 测试使用feign远程上传文件
 * @date 2023/6/14 3:58
 */
@SpringBootTest
public class FeignUploadTest {

    @Resource
    private MediaServiceClient mediaServiceClient;

    @Test
    void test() {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\download\\xczx\\test.html"));
        mediaServiceClient.uploadFile(multipartFile, "course/121.html");
    }
}
