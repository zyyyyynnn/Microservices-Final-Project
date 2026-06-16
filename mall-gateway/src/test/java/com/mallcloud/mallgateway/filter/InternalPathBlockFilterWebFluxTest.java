package com.mallcloud.mallgateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * InternalPathBlockFilter 在 Spring 上下文中的集成测试。
 *
 * 设计目的：Sprint 3.6 单测仅用 MockServerHttpExchange 验证 filter 行为，
 * 未在 Spring 容器中确认：
 *  1) filter 被 @Component 注册为 bean（非死代码）；
 *  2) filter order=-200 早于 JwtAuthFilter，避免后续认证逻辑绕过。
 *
 * 实现说明：使用 {@link SpringBootTest} 加载最小上下文（仅 InternalPathBlockFilter），
 * 验证 bean 加载与 order 语义。HTTP 行为由 {@link InternalPathBlockFilterTest}
 * 的 13 个 MockServerWebExchange 单测覆盖（含 Sprint 3.7 新增的 6 个通用 internal
 * 路径正则测试与 1 个 order 验证测试）。该模式避免引入 Spring Cloud Gateway 完整
 * 自动装配（依赖 Nacos/Redis/9100 等），仅做 bean 注册与配置元数据级验证。
 *
 * 不引入 Spring Security 测试支持；纯过滤器元数据验证。
 */
@SpringBootTest(classes = InternalPathBlockFilterWebFluxTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class InternalPathBlockFilterWebFluxTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private InternalPathBlockFilter internalPathBlockFilter;

    @TestConfiguration
    @Import(InternalPathBlockFilter.class)
    static class TestConfig {
        // 仅导入 InternalPathBlockFilter；其他 Spring Boot 自动配置由 @SpringBootTest 提供
    }

    @Test
    @DisplayName("Spring 上下文：filter 注册为 bean 且 order 来自 GatewayFilterOrders 常量")
    void filterIsRegisteredAsBean() {
        InternalPathBlockFilter bean = applicationContext.getBean(InternalPathBlockFilter.class);
        assertNotNull(bean, "filter must be registered as a Spring bean");
        assertSame(internalPathBlockFilter, bean, "autowired instance must equal context bean");
        // Sprint 3.8：order 来自 GatewayFilterOrders 常量，不再硬编码 -200
        assertEquals(GatewayFilterOrders.INTERNAL_PATH_BLOCK_FILTER_ORDER, bean.getOrder(),
                "filter order must equal GatewayFilterOrders.INTERNAL_PATH_BLOCK_FILTER_ORDER (-200)");
        // 相对顺序断言：必须早于 JWT 鉴权
        assertTrue(bean.getOrder() < GatewayFilterOrders.JWT_AUTH_FILTER_ORDER,
                "INTERNAL_PATH_BLOCK_FILTER_ORDER must be < JWT_AUTH_FILTER_ORDER so internal path is blocked before auth");
    }

    @Test
    @DisplayName("Spring 上下文：filter 实现 Ordered 接口")
    void filterImplementsOrdered() {
        assertTrue(internalPathBlockFilter instanceof Ordered,
                "filter must implement Ordered so Spring can apply order semantics");
    }

    @Test
    @DisplayName("Sprint 3.7 通用正则暴露给集成测试用")
    void genericPatternIsExposed() {
        // InternalPathBlockFilter.getInternalPathPattern() 暴露静态 Pattern 供其他模块复用
        // 这里验证：通用正则确实匹配 /api/v1/{业务段}/internal/**
        java.util.regex.Pattern p = InternalPathBlockFilter.getInternalPathPattern();
        assertTrue(p.matcher("/api/v1/users/internal/1001").matches(),
                "generic pattern must match /api/v1/users/internal/1001");
        assertTrue(p.matcher("/api/v1/products/internal/1001").matches(),
                "generic pattern must match /api/v1/products/internal/1001 (defense in depth)");
        assertTrue(p.matcher("/api/v1/orders/internal/x").matches(),
                "generic pattern must match /api/v1/orders/internal/x (defense in depth)");
        // 不应误伤普通业务路径
        assertTrue(!p.matcher("/api/v1/users/me/addresses").matches(),
                "generic pattern must NOT match /api/v1/users/me/addresses");
        assertTrue(!p.matcher("/api/v1/orders/SO123").matches(),
                "generic pattern must NOT match /api/v1/orders/SO123");
    }

    @Test
    @DisplayName("Sprint 3.7 兼容保留：users 特定正则（语义锚点）")
    void usersInternalSpecificPatternIsExposed() {
        // 历史上 mall-user 是第一个接入 internal 保护的服务，保留显式 Pattern
        java.util.regex.Pattern p = InternalPathBlockFilter.getUsersInternalPathPattern();
        assertTrue(p.matcher("/api/v1/users/internal/1001/addresses/1").matches(),
                "users-specific pattern must match /api/v1/users/internal/1001/addresses/1");
        assertTrue(!p.matcher("/api/v1/products/internal/1001").matches(),
                "users-specific pattern must NOT match non-user paths");
    }
}
