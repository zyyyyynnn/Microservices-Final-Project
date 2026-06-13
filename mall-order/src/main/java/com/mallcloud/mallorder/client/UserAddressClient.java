package com.mallcloud.mallorder.client;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallorder.client.dto.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 订单服务 → 用户服务：内部地址查询。
 * 用于订单创建时把地址快照写入 addressJson，保证订单详情页能展示完整收货信息。
 */
@FeignClient(name = "mall-user", contextId = "userAddressClient",
        fallbackFactory = UserAddressClientFallbackFactory.class)
public interface UserAddressClient {

    @GetMapping("/api/v1/users/internal/{userId}/addresses/{addressId}")
    Result<AddressDTO> getInternalAddress(@PathVariable("userId") Long userId,
                                          @PathVariable("addressId") Long addressId);
}
