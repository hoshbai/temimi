package com.temimi.exception;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {
    
    private final int code;
    private final BusinessErrorCode errorCode;
    
    // 兼容现有代码的构造函数
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorCode = null;
    }
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.errorCode = null;
    }
    
    public BusinessException(BusinessErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }
    
    public BusinessException(BusinessErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }
    
    public BusinessException(BusinessErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }
    
    public int getCode() {
        return code;
    }
    
    public BusinessErrorCode getErrorCode() {
        return errorCode;
    }
}