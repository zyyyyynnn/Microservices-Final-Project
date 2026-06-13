package com.mallcloud.mallorder.client;

import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallorder.client.dto.AddressDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * UserAddressClient 降级工厂：
 * - mall-user 不可用时，订单仍能创建（仅丢失地址快照细节）
 * - OrderServiceImpl 会在收到降级响应时回退到 addressId-only 快照，保证主流程不阻塞
 */
@Slf4j
@Component
public class UserAddressClientFallbackFactory implements FallbackFactory<UserAddressClient> {

    @Override
    public UserAddressClient create(Throwable cause) {
        return new UserAddressClient() {
            @Override
            public Result<AddressDTO> getInternalAddress(Long userId, Long addressId) {
                log.warn("调用 mall-user 查询地址失败 userId={} addressId={} cause={}",
                        userId, addressId, cause == null ? "null" : cause.getMessage());
                return Result.error(ErrorCode.REMOTE_CALL_ERROR.getCode(),
                        "调用 mall-user 查询地址失败");
            }
        };
    }
}
