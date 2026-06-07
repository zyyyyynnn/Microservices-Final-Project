package com.mallcloud.mallinventory.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class LockStockDTO {
    private String orderNo;
    private List<LockItemDTO> items;
}
