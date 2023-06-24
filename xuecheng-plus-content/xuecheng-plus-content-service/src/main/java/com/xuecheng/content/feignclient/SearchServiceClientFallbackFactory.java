package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yzhans
 * @version 1.0
 * @description 搜索服务远程调用
 * @date 2023/6/24 3:46
 */
@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {


    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.debug("调用搜索发生熔断走降级方法,熔断异常:{}", throwable.getMessage());
                //如果走了降级，返回false
                return false;
            }
        };
    }
}
