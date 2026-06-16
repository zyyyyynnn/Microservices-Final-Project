package com.mallcloud.mallcommon.feign;

import com.mallcloud.mallcommon.constant.CommonConstants;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeignUserInterceptorTest {

    private final FeignUserInterceptor interceptor = new FeignUserInterceptor();

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CommonConstants.HEADER_USER_ID, "1001");
        request.addHeader(CommonConstants.HEADER_USER_ROLES, "USER,ADMIN");
        request.addHeader(CommonConstants.HEADER_TRACE_ID, "trace-xyz");
        request.addHeader(CommonConstants.HEADER_INTERNAL_TOKEN, "dev-internal-token");
        request.addHeader("X-Internal-Foo", "client-claim");
        request.addHeader("Authorization", "Bearer abc");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void forUserContextHeadersAreForwarded() {
        RequestTemplate template = new RequestTemplate().method("GET").uri("/api/v1/foo");

        interceptor.apply(template);

        assertEquals("1001", template.headers().get(CommonConstants.HEADER_USER_ID).toArray()[0]);
        assertEquals("USER,ADMIN", template.headers().get(CommonConstants.HEADER_USER_ROLES).toArray()[0]);
        assertEquals("trace-xyz", template.headers().get(CommonConstants.HEADER_TRACE_ID).toArray()[0]);
    }

    @Test
    void authorizationHeaderIsNotCopied() {
        RequestTemplate template = new RequestTemplate().method("GET").uri("/api/v1/foo");

        interceptor.apply(template);

        assertNull(template.headers().get("Authorization"));
    }

    @Test
    void internalTokenHeaderIsNotCopied() {
        RequestTemplate template = new RequestTemplate().method("GET").uri("/api/v1/foo");

        interceptor.apply(template);

        assertNull(template.headers().get(CommonConstants.HEADER_INTERNAL_TOKEN));
    }

    @Test
    void arbitraryXInternalHeaderIsNotCopied() {
        RequestTemplate template = new RequestTemplate().method("GET").uri("/api/v1/foo");

        interceptor.apply(template);

        assertNull(template.headers().get("X-Internal-Foo"));
        assertTrue(template.headers().keySet().stream()
                .noneMatch(name -> name != null && name.toLowerCase().startsWith("x-internal-")));
    }

    @Test
    void noInboundRequestLeavesTemplateUnchanged() {
        RequestContextHolder.resetRequestAttributes();
        RequestTemplate template = new RequestTemplate().method("GET").uri("/api/v1/foo");

        interceptor.apply(template);

        assertTrue(template.headers().isEmpty() || template.headers().equals(Collections.emptyMap()));
    }
}
