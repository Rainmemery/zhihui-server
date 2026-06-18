package com.zhihui.common.exception;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException {
    private int code;
    private String message;
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
