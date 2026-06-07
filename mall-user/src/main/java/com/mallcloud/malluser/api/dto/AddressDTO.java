package com.mallcloud.malluser.api.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Boolean isDefault;
}
