package com.mallcloud.mallinventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.mallinventory.domain.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface StockMapper extends BaseMapper<Stock> {

    @Update("UPDATE stock SET locked = locked + #{qty}, available = available - #{qty} " +
            "WHERE sku_id = #{skuId} AND available >= #{qty}")
    int lockStock(@Param("skuId") Long skuId, @Param("qty") Integer qty);

    @Update("UPDATE stock SET locked = locked - #{qty}, total = total - #{qty} " +
            "WHERE sku_id = #{skuId} AND locked >= #{qty}")
    int deductStock(@Param("skuId") Long skuId, @Param("qty") Integer qty);

    @Update("UPDATE stock SET locked = locked - #{qty}, available = available + #{qty} " +
            "WHERE sku_id = #{skuId} AND locked >= #{qty}")
    int releaseStock(@Param("skuId") Long skuId, @Param("qty") Integer qty);
}
