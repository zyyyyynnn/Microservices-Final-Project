package com.mallcloud.mallauth.client;

import com.mallcloud.mallauth.api.dto.UserInternalDTO;
import com.mallcloud.mallcommon.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 用户服务 Feign 客户端熔断回调工厂
 */
@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public Result<UserInternalDTO> getUserById(Long userId) {
                log.error("调用 mall-user 服务获取用户信息失败, userId: {}, error: {}", userId, cause.getMessage());
                // 暂时提供降级数据，允许 mall-auth 独立运行
                UserInternalDTO mockUser = new UserInternalDTO();
                mockUser.setId(userId);
                mockUser.setNickname("暂无昵称");
                mockUser.setAvatar("https://mallcloud.com/default-avatar.png");
                return Result.ok(mockUser);
            }
        };
    }
}
