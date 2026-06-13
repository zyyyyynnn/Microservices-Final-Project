package com.mallcloud.mallorder.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户地址 DTO（Feign 调用 mall-user 内部地址接口返回）。
 * 字段与 mall-user/api/vo/AddressVO 保持一致。
 */
@Data
public class AddressDTO {
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
