package com.mallcloud.mallauth.api.dto;

import lombok.Data;

/**
 * 刷新 Token 请求参数
 */
@Data
public class RefreshTokenDTO {
    private String refreshToken;
}
