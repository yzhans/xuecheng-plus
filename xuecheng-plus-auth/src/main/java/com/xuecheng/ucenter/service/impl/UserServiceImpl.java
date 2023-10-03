package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description 自定义用户详情服务
 * @date 2023/7/22 2:26
 */
@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {

    @Resource
    XcUserMapper xcUserMapper;

    @Resource
    XcMenuMapper xcMenuMapper;

    @Resource
    ApplicationContext applicationContext;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //将传入的json转成AuthParamsDto对象
        AuthParamsDto authParamsDto = null;

        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证参数不符合要求");
        }

        //通过请求参数获取认证类型
        String authType = authParamsDto.getAuthType();

        //根据认证的类型,从springioc中取出相应的类型
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);

        //调用统一的execute方法完成认证
        XcUserExt userExt = authService.execute(authParamsDto);

        return getUserPrincipal(userExt);
    }

    /***
    * @description 查询用户信息
    * @param xcUser 用户id，主键
    * @return org.springframework.security.core.userdetails.UserDetails
    * @author xoo
    * @date 2023/9/22 18:00
    */
    public UserDetails getUserPrincipal(XcUserExt xcUser) {
        String password = xcUser.getPassword();
        //权限
        String[] authorities = {"test"};

        //查询用户权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        if (!xcMenus.isEmpty()) {
            List<String> permissions = new ArrayList<>();
            xcMenus.forEach(m -> {
                //拿到了用户的权限标识符
                permissions.add(m.getCode());
            });
            //将list转成数组
            authorities = permissions.toArray(new String[0]);
        }

        xcUser.setPassword(null);
        String json = JSON.toJSONString(xcUser);
        return User.withUsername(json).password(password).authorities(authorities).build();
    }
}
