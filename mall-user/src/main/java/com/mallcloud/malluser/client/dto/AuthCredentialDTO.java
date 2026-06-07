package com.mallcloud.malluser.client.dto;

import lombok.Data;

@Data
public class AuthCredentialDTO {
    private Long userId;
    private String username;
    private String phone;
    private String password;
}
