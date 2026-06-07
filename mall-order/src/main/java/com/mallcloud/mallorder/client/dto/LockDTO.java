package com.mallcloud.mallorder.client.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockDTO {
    private Long skuId;
    private Integer quantity;
}
