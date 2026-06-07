package com.mallcloud.mallorder.client;

import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallorder.client.dto.SkuDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        return new ProductClient() {
            @Override
            public Result<SkuDTO> getSku(Long skuId) {
                log.error("调用 mall-product 查询 SKU 失败, skuId={}, error={}", skuId, error(cause), cause);
                return Result.error(ErrorCode.REMOTE_CALL_ERROR.getCode(), "调用 mall-product 查询 SKU 失败");
            }
        };
    }

    private String error(Throwable cause) {
        return cause == null ? "unknown" : cause.getMessage();
    }
}
