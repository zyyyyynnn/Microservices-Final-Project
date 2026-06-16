package com.mallcloud.mallcommon.exception;

import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.response.Result;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    void directBizExceptionReturnsOriginalCodeAndMessage() {
        BizException biz = new BizException(40402, "秒杀已结束");
        request.setRequestURI("/api/v1/seckill/1");

        Result<Void> result = handler.handleBiz(biz, request);

        assertEquals(40402, result.getCode());
        assertEquals("秒杀已结束", result.getMessage());
    }

    @Test
    void singleLevelWrappedBizExceptionIsUnwrapped() {
        BizException biz = new BizException(40100, "库存不足或锁定失败");
        RuntimeException wrapper = new RuntimeException("feign invoke failed", biz);
        request.setRequestURI("/api/v1/orders");

        ResponseEntity<Result<Void>> response = handler.handleAll(wrapper, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Result<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(40100, body.getCode());
        assertEquals("库存不足或锁定失败", body.getMessage());
    }

    @Test
    void multiLevelWrappedBizExceptionIsUnwrapped() {
        BizException biz = new BizException(40402, "秒杀已结束");
        RuntimeException w1 = new RuntimeException("seata aop", biz);
        RuntimeException w2 = new RuntimeException("global tx", w1);
        RuntimeException w3 = new RuntimeException("controller entry", w2);
        request.setRequestURI("/api/v1/seckill/1");

        ResponseEntity<Result<Void>> response = handler.handleAll(w3, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Result<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(40402, body.getCode());
        assertEquals("秒杀已结束", body.getMessage());
    }

    @Test
    void exceptionWithoutBizCauseFallsBackToSystemError() {
        RuntimeException plain = new RuntimeException("plain boom");
        request.setRequestURI("/api/v1/orders");

        ResponseEntity<Result<Void>> response = handler.handleAll(plain, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Result<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), body.getCode());
        assertTrue(body.getMessage().contains("系统繁忙"));
    }

    @Test
    void causeChainCycleDoesNotLoop() {
        BizException biz = new BizException(40101, "异常测试");
        CyclicA a = new CyclicA();
        CyclicB b = new CyclicB();
        a.cause = b;
        b.cause = a;
        a.cause = null;
        a.cause = b;
        b.cause = a;

        request.setRequestURI("/api/v1/test");
        long start = System.currentTimeMillis();
        ResponseEntity<Result<Void>> response = handler.handleAll(a, request);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 1000, "cause chain traversal should not loop");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deeplyNestedCauseBeyondDepthLimitFallsBackToSystemError() {
        RuntimeException current = new RuntimeException("wrap-root",
                new BizException(40001, "deepest biz"));
        for (int i = 0; i < 20; i++) {
            current = new RuntimeException("wrap-" + i, current);
        }
        request.setRequestURI("/api/v1/test");

        ResponseEntity<Result<Void>> response = handler.handleAll(current, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), response.getBody().getCode());
    }

    private static class CyclicA extends RuntimeException {
        Throwable cause;
        @Override
        public synchronized Throwable getCause() { return cause; }
    }

    private static class CyclicB extends RuntimeException {
        Throwable cause;
        @Override
        public synchronized Throwable getCause() { return cause; }
    }
}
