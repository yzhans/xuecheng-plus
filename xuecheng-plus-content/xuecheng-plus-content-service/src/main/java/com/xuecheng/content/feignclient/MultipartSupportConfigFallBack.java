package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/6/14 6:24
 */
public class MultipartSupportConfigFallBack implements MediaServiceClient {

    @Override
    public String uploadFile(MultipartFile upload, String objectName) {
        return null;
    }
}
