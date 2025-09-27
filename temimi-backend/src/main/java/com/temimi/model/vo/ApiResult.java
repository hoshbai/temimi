package com.temimi.model.vo;

import lombok.Data;

/**
 * 统一API响应结果封装
 * code: 200 成功, 非200 失败
 * message: 响应消息
 * data: 响应的具体数据
 */
@Data
public class ApiResult<T> {
    private Integer code;
    private String message;
    private T data;

    // 成功响应 (无数据)
    public static <T> ApiResult<T> success() {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }
    // 成功响应 (带提示信息)
    public static <T> ApiResult<T> success(String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage(message);
        return result;
    }
    // 成功响应 (带数据)
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 失败响应 (自定义消息)
    public static <T> ApiResult<T> error(String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    // 失败响应 (自定义code和message)
    public static <T> ApiResult<T> error(Integer code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}