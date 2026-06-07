package com.mallcloud.mallinventory.service;

import com.mallcloud.mallinventory.api.dto.LockStockDTO;
import com.mallcloud.mallinventory.api.dto.OrderNoDTO;

public interface InventoryService {
    void lock(LockStockDTO dto);
    void deduct(OrderNoDTO dto);
    void release(OrderNoDTO dto);
    int reconcileStock();
}
