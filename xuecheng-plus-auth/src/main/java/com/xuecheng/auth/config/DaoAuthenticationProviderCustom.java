package com.xuecheng.auth.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author xoo
 * @version 1.0
 * @description 重写了DaoAuthenticationProvider的校验的密码的方法,因为我们统一认证入口,有一些认证方式不需要校验密码
 * @date 2023/9/22 17:00
 */
@Component
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {

    @Resource
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }
}
