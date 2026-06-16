package com.mallcloud.mallgateway.filter;

/**
 * Gateway 全局过滤器 order 常量集中管理。
 *
 * 背景（Sprint 3.7 §9.10.7 未解决项）：
 *   InternalPathBlockFilter.getOrder() == -200、JwtAuthFilter.getOrder() == -100
 *   之前是各自硬编码魔法值，filter 间相对顺序仅靠约定，缺 schema 约束。
 *   Sprint 3.8 引入本常量类集中管理，使相对顺序有显式表达。
 *
 * 顺序约定（值越小越先执行）：
 *   1) INTERNAL_PATH_BLOCK_FILTER_ORDER (-200)：阻断 /api/v1/{seg}/internal/**
 *      + 净化 X-Internal-* header；必须早于 JWT 鉴权，避免给未授权的 internal
 *      请求打上 userId 头再放行到 mall-user；
 *   2) JWT_AUTH_FILTER_ORDER (-100)：白名单放行 + JWT 解析 + 写 X-User-Id /
 *      X-User-Roles 头。
 *
 * 如未来新增全局过滤器，请按本常量类的相对顺序插入，并在 §9.X 章节记录理由。
 *
 * 重要：实际 order 值在 Sprint 3.8 与 Sprint 3.6 保持一致（-200 / -100），
 * 不随意调整；如需调整 order，必须同步更新本常量 + 所有引用方 + 测试断言。
 */
public final class GatewayFilterOrders {

    /**
     * 内部路径阻断 + 内部 header 净化过滤器 order。
     * 必须早于 {@link #JWT_AUTH_FILTER_ORDER}。
     */
    public static final int INTERNAL_PATH_BLOCK_FILTER_ORDER = -200;

    /**
     * JWT 鉴权过滤器 order。
     * 必须晚于 {@link #INTERNAL_PATH_BLOCK_FILTER_ORDER}。
     */
    public static final int JWT_AUTH_FILTER_ORDER = -100;

    private GatewayFilterOrders() {
        // 工具类，禁止实例化
    }
}
