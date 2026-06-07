package com.mallcloud.mallauth.service;

import com.mallcloud.mallauth.api.dto.LoginDTO;
import com.mallcloud.mallauth.api.dto.RefreshTokenDTO;
import com.mallcloud.mallauth.api.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);
    
    /**
     * 刷新 Token
     */
    LoginVO refresh(RefreshTokenDTO refreshDTO);
    
    /**
     * 登出
     */
    void logout(String token);
}
