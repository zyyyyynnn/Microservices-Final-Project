package com.mallcloud.malluser.api.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AddressVO {
    private Long id;
    private Long userId;
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;
    private LocalDateTime gmtCreate;
}
