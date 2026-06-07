package com.mallcloud.malluser.api.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String phone;
    private String nickname;
    private String avatar;
    private String email;
    private Integer status;
    private LocalDateTime gmtCreate;
}
