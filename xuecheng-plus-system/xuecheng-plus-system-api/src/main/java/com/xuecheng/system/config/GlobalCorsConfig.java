package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/2 21:03
 */
@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter getCorsFilter() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();
         //添加那些方法可以跨域
        corsConfiguration.addAllowedMethod("*");
        //允许那些请求进行跨域 也可以具体指定
        corsConfiguration.addAllowedOrigin("*");
        //所有头信息全部放行
        corsConfiguration.addAllowedHeader("*");
        //允许跨域发送cookie
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsFilter(source);
    }

}
