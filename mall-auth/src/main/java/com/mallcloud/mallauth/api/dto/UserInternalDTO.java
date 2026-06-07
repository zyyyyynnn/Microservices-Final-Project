package com.mallcloud.mallauth.api.dto;

import lombok.Data;

/**
 * 内部调用用户数据
 */
@Data
public class UserInternalDTO {
    private Long id;
    private String nickname;
    private String avatar;
}
