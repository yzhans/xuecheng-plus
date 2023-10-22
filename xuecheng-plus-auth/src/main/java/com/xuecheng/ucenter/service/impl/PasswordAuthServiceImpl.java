package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xoo
 * @version 1.0
 * @description 密码认证实现
 * @date 2023/9/22 17:20
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Resource
    XcUserMapper xcUserMapper;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {

        //验证码
        String checkcode = authParamsDto.getCheckcode();

        //验证码对应的key
        String checkcodekey = authParamsDto.getCheckcodekey();

        //if (StringUtils.isBlank(checkcodekey) || StringUtils.isBlank(checkcode)) {
        //    throw new RuntimeException("验证码为空");
        //}
        //
        ////远程调用验证码服务校验验证码
        //Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        //
        //if (verify == null || !verify) {
        //    throw new RuntimeException("验证码输入错误");
        //}

        //查询user账号用户是否存在
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, authParamsDto.getUsername()));
        //查询到用户不存在，要返回null即可, spring security框架抛出异常用户不存在
        if (xcUser == null) {
            throw new RuntimeException("账号不存在");
        }
        //查到用户密码
        String password = xcUser.getPassword();

        //查询网站传入的密码 判断对比
        String websitePassword = authParamsDto.getPassword();

        //校验密码
        boolean matches = passwordEncoder.matches(websitePassword, password);
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);

        return xcUserExt;
    }
}
