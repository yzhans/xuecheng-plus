package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @author xoo
 * @version 1.0
 * @description 统一的认证接口
 * @date 2023/9/22 17:16
 */
public interface AuthService {

    /***
     * @description 认证方法
     * @param authParamsDto 认证参数
     * @return com.xuecheng.ucenter.model.dto.XcUserExt
     * @author xoo
     * @date 2023/9/22 17:18
     */
    XcUserExt execute(AuthParamsDto authParamsDto);

}
