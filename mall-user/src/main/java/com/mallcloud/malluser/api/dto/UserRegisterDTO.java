package com.mallcloud.malluser.api.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String phone;
    private String password;
    private String smsCode;
}
