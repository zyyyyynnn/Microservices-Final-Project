package com.mallcloud.mallcommon.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果
 *
 * @param <T> 数据类型
 * @author zhangsan
 * @since 2026-03-01
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_FAIL = 500;

    private int code;
    private String message;
    private T data;
    private String traceId;
    private long timestamp = System.currentTimeMillis();

    public Result() {}

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return new Result<>(CODE_SUCCESS, "ok", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(CODE_SUCCESS, "ok", data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(CODE_SUCCESS, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(CODE_FAIL, message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public boolean isSuccess() {
        return this.code == CODE_SUCCESS;
    }
}
