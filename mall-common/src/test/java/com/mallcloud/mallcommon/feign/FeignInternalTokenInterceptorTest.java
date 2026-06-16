package com.mallcloud.mallcommon.feign;

import com.mallcloud.mallcommon.config.InternalAuthProperties;
import com.mallcloud.mallcommon.constant.CommonConstants;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeignInternalTokenInterceptorTest {

    private RequestTemplate template() {
        return new RequestTemplate().method("GET").uri("/api/v1/foo");
    }

    @Test
    void tokenIsInjectedWhenNotPresent() {
        InternalAuthProperties props = new InternalAuthProperties();
        props.setToken("dev-internal-token");
        FeignInternalTokenInterceptor interceptor = new FeignInternalTokenInterceptor(props);

        RequestTemplate template = template();
        interceptor.apply(template);

        assertEquals("dev-internal-token",
                template.headers().get(CommonConstants.HEADER_INTERNAL_TOKEN).toArray()[0]);
    }

    @Test
    void existingExplicitTokenIsNotOverwritten() {
        InternalAuthProperties props = new InternalAuthProperties();
        props.setToken("dev-internal-token");
        FeignInternalTokenInterceptor interceptor = new FeignInternalTokenInterceptor(props);

        RequestTemplate template = template();
        template.header(CommonConstants.HEADER_INTERNAL_TOKEN, "caller-supplied");
        interceptor.apply(template);

        Object[] values = template.headers().get(CommonConstants.HEADER_INTERNAL_TOKEN).toArray();
        assertEquals(1, values.length);
        assertEquals("caller-supplied", values[0]);
    }

    @Test
    void blankTokenIsNotInjected() {
        InternalAuthProperties props = new InternalAuthProperties();
        props.setToken("   ");
        FeignInternalTokenInterceptor interceptor = new FeignInternalTokenInterceptor(props);

        RequestTemplate template = template();
        interceptor.apply(template);

        assertNull(template.headers().get(CommonConstants.HEADER_INTERNAL_TOKEN));
    }

    @Test
    void nullTokenIsNotInjected() {
        InternalAuthProperties props = new InternalAuthProperties();
        props.setToken(null);
        FeignInternalTokenInterceptor interceptor = new FeignInternalTokenInterceptor(props);

        RequestTemplate template = template();
        interceptor.apply(template);

        assertNull(template.headers().get(CommonConstants.HEADER_INTERNAL_TOKEN));
    }

    @Test
    void nullPropertiesIsTolerated() {
        FeignInternalTokenInterceptor interceptor = new FeignInternalTokenInterceptor(null);

        RequestTemplate template = template();
        interceptor.apply(template);

        assertTrue(template.headers().isEmpty()
                || !template.headers().containsKey(CommonConstants.HEADER_INTERNAL_TOKEN));
    }
}
