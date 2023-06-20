package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yzhans
 * @version 1.0
 * @description 远程调用媒资服务接口
 * @date 2023/6/14 3:53
 */
//使用fallback定义降级类是无法拿到熔断异常，
//@FeignClient(value = "media-api", configuration = {MultipartSupportConfig.class},fallback = MultipartSupportConfigFallBack.class)
@FeignClient(value = "media-api", configuration = {MultipartSupportConfig.class},fallbackFactory = MultipartSupportConfigFallbackFactory.class)
public interface MediaServiceClient {

    @RequestMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("filedata") MultipartFile upload, @RequestParam(value = "objectName", required = false) String objectName);


}
