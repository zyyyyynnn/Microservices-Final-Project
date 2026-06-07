package com.mallcloud.mallauth.api.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录响应视图对象
 */
@Data
@Builder
public class LoginVO {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfoVO userInfo;
}
