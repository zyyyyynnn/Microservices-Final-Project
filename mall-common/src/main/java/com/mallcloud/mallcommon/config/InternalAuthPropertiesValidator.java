package com.mallcloud.mallcommon.config;

import com.mallcloud.mallcommon.exception.BizException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * internal token 配置在非本地环境下的 fail-fast 校验。
 *
 * 规则：
 *  - active profile 含 dev / local / test → 允许 dev 默认值、blank、空（仅供本地开发）
 *  - active profile 仅含 prod / staging / full 等 → 强制要求 mall.internal.token
 *    显式非空，且不得等于 "dev-internal-token" 默认值
 *
 * 触发时机：mall-common 自动装配时（任何引用 mall-common 的服务启动阶段）。
 * 设计目标：把 "生产环境沿用 dev 默认 token" 这类误用卡在启动阶段，而不是运行时被攻击。
 *
 * 注意：fail-fast 抛 BizException(401, ...)，业务码沿用服务间鉴权失败码；由 GlobalExceptionHandler
 * 在启动失败时输出结构化响应。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InternalAuthPropertiesValidator {

    static final String DEV_DEFAULT_TOKEN = "dev-internal-token";

    private static final Set<String> LOCAL_PROFILES = new HashSet<>(Arrays.asList("dev", "local", "test", "default"));

    private final InternalAuthProperties properties;
    private final Environment environment;

    @PostConstruct
    public void validate() {
        Set<String> active = new HashSet<>(Arrays.asList(environment.getActiveProfiles()));
        if (active.isEmpty()) {
            active.add("default");
        }
        boolean isLocal = active.stream().allMatch(LOCAL_PROFILES::contains);
        if (isLocal) {
            log.info("[InternalAuth] local profile={}，允许 dev 默认 token", active);
            return;
        }

        String token = properties == null ? null : properties.getToken();
        if (!StringUtils.hasText(token)) {
            throw new BizException(401,
                    "[InternalAuth] 非本地环境（profile=" + active + "）必须显式配置 mall.internal.token（通过 Nacos 或环境变量 MALL_INTERNAL_TOKEN）");
        }
        if (DEV_DEFAULT_TOKEN.equals(token)) {
            throw new BizException(401,
                    "[InternalAuth] 非本地环境（profile=" + active + "）禁止使用 dev 默认 token，请通过 Nacos / 环境变量覆盖");
        }
        log.info("[InternalAuth] 非本地环境（profile={}）使用自定义 token，长度={}", active, token.length());
    }
}
