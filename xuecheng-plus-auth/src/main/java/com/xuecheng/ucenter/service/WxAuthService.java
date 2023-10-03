package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @author xoo
 * @version 1.0
 * @description 微信认证接口
 * @date 2023/9/24 11:52
 */
public interface WxAuthService {

    /***
    * @description 微信扫码认证拿到授权码,申请令牌,携带令牌查询用户信息、保存用户信息到数据库
    * @param code 授权码
    * @return com.xuecheng.ucenter.model.po.XcUser
    * @author xoo
    * @date 2023/9/24 12:02
    */
    public XcUser wxAuth(String code);

}
