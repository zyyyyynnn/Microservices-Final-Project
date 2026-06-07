package com.mallcloud.mallorder.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockStockDTO {
    private String orderNo;
    private List<LockDTO> items;
}
