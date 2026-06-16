package com.mallcloud.mallcommon.exception;

import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理
 *
 * @author zhangsan
 * @since 2026-03-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e, HttpServletRequest req) {
        log.warn("[BizException] path={} code={} msg={}", req.getRequestURI(), e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegal(IllegalArgumentException e) {
        log.warn("[IllegalArgument] {}", e.getMessage());
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 静态资源 / 未知路径交给全局处理，避免被误归类为 SystemError。
     * 仅返回 404 业务码 + 路径信息，不再触发系统告警日志。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResource(NoResourceFoundException e, HttpServletRequest req) {
        log.info("[NoResource] path={} resource={}", req.getRequestURI(), e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(404, "资源不存在: " + e.getResourcePath()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleAll(Exception e, HttpServletRequest req) {
        // 沿 cause 链查找被包装的 BizException（Seata AOP、Feign 异常包装、事务回滚等场景）。
        // 命中：恢复业务码与 message，按 BizException 语义返回（不当作系统错误）。
        // 未命中：仍按系统错误兜底（HTTP 500 + code=10003）。
        // 深度上限 8，防止循环引用 / 异常链构造异常导致死循环。
        BizException biz = findBizException(e, 8);
        if (biz != null) {
            log.warn("[BizWrapped] path={} code={} msg={} causeType={}",
                    req.getRequestURI(), biz.getCode(), biz.getMessage(),
                    e.getClass().getName());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Result.error(biz.getCode(), biz.getMessage()));
        }
        log.error("[SystemError] path={}", req.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统繁忙，请稍后重试"));
    }

    /**
     * 沿 cause 链递归查找 BizException，最多 8 层。
     * 用于在 catch-all 异常处理器中恢复被 Seata/Feign/事务框架包装的业务异常。
     */
    private BizException findBizException(Throwable ex, int maxDepth) {
        if (ex == null || maxDepth <= 0) {
            return null;
        }
        if (ex instanceof BizException biz) {
            return biz;
        }
        // 处理 InvocationTargetException 等常见包装（反射、代理调用栈）
        Throwable cause = ex.getCause();
        if (cause == null || cause == ex) {
            return null;
        }
        return findBizException(cause, maxDepth - 1);
    }
}
