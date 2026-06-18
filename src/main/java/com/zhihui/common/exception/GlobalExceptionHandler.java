package com.zhihui.common.exception;

import com.zhihui.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. 自定义业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("【业务异常】code:{}, msg:{}", e.getCode(), e.getMessage(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    // 2. @Valid 请求体JSON字段校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<FieldError> errorList = e.getBindingResult().getFieldErrors();
        StringBuilder msgSb = new StringBuilder();
        for (FieldError err : errorList) {
            msgSb.append(err.getField()).append("：").append(err.getDefaultMessage()).append("；");
        }
        String errMsg = msgSb.toString();
        log.error("【请求参数校验异常】{}", errMsg, e);
        return Result.fail(ErrorCode.PARAM_ERROR.getCode(), errMsg);
    }

    // 3. 缺少必传请求参数（@RequestParam 缺失）
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        String msg = "缺少必填参数：" + e.getParameterName();
        log.error("【缺少请求参数】{}", msg, e);
        return Result.fail(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    // 4. JSON解析失败（请求体格式错误、类型不匹配、空body等）
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        String msg = "请求JSON格式错误或数据类型不匹配";
        log.error("【JSON解析异常】", e);
        return Result.fail(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    // 5. 兜底捕获所有未知异常，只打印堆栈，前端统一返回服务异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("【系统未知异常】完整堆栈：", e);
        return Result.fail(ErrorCode.SERVER_ERROR.getCode(), ErrorCode.SERVER_ERROR.getMessage());
    }
}
