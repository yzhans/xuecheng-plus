package com.xuecheng.content.feignclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author yzhans
 * @version 1.0
 * @description 搜索服务远程接口
 * @date 2023/6/24 3:41
 */
@FeignClient(value = "search",fallbackFactory = SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {

    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);

}
