package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/6/14 6:27
 */
@Slf4j
@Component
public class MultipartSupportConfigFallbackFactory implements FallbackFactory<MediaServiceClient> {
    //可以拿到异常信息
    @Override
    public MediaServiceClient create(Throwable throwable) {
        //出现熔断 上游服务就调用此方法执行降级逻辑
        return new MediaServiceClient() {
            @Override
            public String uploadFile(MultipartFile upload, String objectName) {
                log.debug("远程调用上传文件的接口发生熔断：{}", throwable.toString(), throwable);
                return null;
            }
        };
    }
}
