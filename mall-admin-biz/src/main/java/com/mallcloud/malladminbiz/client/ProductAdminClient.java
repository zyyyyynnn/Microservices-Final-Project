package com.mallcloud.malladminbiz.client;

import com.mallcloud.malladminbiz.api.dto.AdminProductQueryDTO;
import com.mallcloud.malladminbiz.api.vo.AdminProductVO;
import com.mallcloud.malladminbiz.client.dto.ProductStatsDTO;
import com.mallcloud.mallcommon.response.PageData;
import com.mallcloud.mallcommon.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 商品后台 Feign
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@FeignClient(name = "mall-product")
public interface ProductAdminClient {

    @GetMapping("/internal/admin/products/stats")
    Result<ProductStatsDTO> getProductStats();

    @GetMapping("/internal/admin/products")
    Result<PageData<AdminProductVO>> listProducts(@SpringQueryMap AdminProductQueryDTO query);
}
