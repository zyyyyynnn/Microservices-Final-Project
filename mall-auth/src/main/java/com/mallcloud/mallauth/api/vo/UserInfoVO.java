package com.mallcloud.mallauth.api.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户信息视图对象
 */
@Data
@Builder
public class UserInfoVO {
    private Long id;
    private String nickname;
    private String avatar;
    private List<String> roles;
}
