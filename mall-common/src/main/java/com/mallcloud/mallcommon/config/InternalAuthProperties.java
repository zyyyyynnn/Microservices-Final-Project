package com.mallcloud.mallcommon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 服务间调用鉴权配置（mall-common 共享）。
 *
 * - 用途：mall-order / mall-pay / mall-admin-biz 等调用 mall-user 等
 *   服务的 /internal/** endpoint 时，Feign 拦截器自动注入 X-Internal-Token header。
 * - mall-user 等被调方在 internal 入口校验该 token。
 * - 严禁把生产密钥硬编码到仓库；dev 默认值仅供本地开发。
 * - 覆盖方式：环境变量 MALL_INTERNAL_TOKEN，或 Nacos 配置 mall.internal.token。
 *
 * 链路：mall-order -> Feign + 拦截器 -> 携带 X-Internal-Token -> mall-user
 *        入口 controller 校验 header == config.token -> 通过 / 抛 401。
 *
 * 同时 Gateway 阻断客户端从 /api/v1/users/internal/** 访问（独立 filter），双层防护。
 */
@Data
@ConfigurationProperties(prefix = "mall.internal")
public class InternalAuthProperties {

    /**
     * 服务间共享 token。dev 默认值仅用于本地；生产必须通过 Nacos / 环境变量覆盖。
     */
    private String token = "dev-internal-token";
}
