package com.temimi.handler;

import com.temimi.exception.BusinessException;
import com.temimi.model.vo.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest; // 修改为jakarta
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResult<String> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<String> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        logger.warn("参数校验失败: {} - {}", request.getRequestURI(), message);
        return ApiResult.error(400, "参数校验失败: " + message);
    }

    /**
     * 绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<String> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        logger.warn("参数绑定失败: {} - {}", request.getRequestURI(), message);
        return ApiResult.error(400, "参数错误: " + message);
    }

    /**
     * 文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        logger.warn("文件上传大小超限: {} - {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(400, "上传文件过大，请选择较小的文件");
    }

    /**
     * 参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<String> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("参数异常: {} - {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(400, e.getMessage());
    }

    /**
     * 未授权异常
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResult<String> handleSecurityException(SecurityException e, HttpServletRequest request) {
        logger.warn("权限异常: {} - {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(401, "无权访问");
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<String> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        logger.error("空指针异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return ApiResult.error(500, "系统内部错误");
    }

    /**
     * 数据库异常
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<String> handleDataAccessException(org.springframework.dao.DataAccessException e, HttpServletRequest request) {
        logger.error("数据库异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return ApiResult.error(500, "数据库操作失败");
    }

    /**
     * 客户端连接中断异常（视频流传输中断）
     * 这是正常现象，用户暂停/跳转视频时会发生，不需要记录错误日志
     */
    @ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    public void handleClientAbortException(org.apache.catalina.connector.ClientAbortException e, HttpServletRequest request) {
        // 只记录DEBUG级别日志，不返回响应（连接已断开）
        logger.debug("客户端连接中断: {} (正常现象)", request.getRequestURI());
        // 不返回ApiResult，因为客户端已断开连接
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<String> handleException(Exception e, HttpServletRequest request) {
        // 记录完整异常信息用于调试
        logger.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);

        // 不向前端暴露具体的系统错误信息
        return ApiResult.error(500, "系统繁忙，请稍后重试");
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<String> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logger.error("运行时异常: {} - {}", request.getRequestURI(), e.getMessage(), e);

        // 如果异常消息比较安全，可以返回给前端
        String message = e.getMessage();
        if (message != null && !message.contains("Exception") && message.length() < 100) {
            return ApiResult.error(500, message);
        }

        return ApiResult.error(500, "操作失败，请稍后重试");
    }
}