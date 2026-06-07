package com.mallcloud.mallauth.client;

import com.mallcloud.mallauth.api.dto.UserInternalDTO;
import com.mallcloud.mallcommon.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端
 */
@FeignClient(name = "mall-user", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {
    
    /**
     * 根据用户 ID 获取内部用户信息
     */
    @GetMapping("/api/v1/users/internal/{userId}")
    Result<UserInternalDTO> getUserById(@PathVariable("userId") Long userId);
}
