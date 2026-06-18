package com.zhihui.common.exception;

/**
 * 全局统一错误码枚举
 */
public enum ErrorCode {
    // 成功
    SUCCESS(200, "操作成功"),
    // 参数错误
    PARAM_ERROR(400, "请求参数非法"),
    // 未登录/token无效
    UNAUTHORIZED(401, "未登录或Token失效"),
    // 权限不足
    FORBIDDEN(403, "无访问权限"),
    // 资源不存在
    NOT_FOUND(404, "资源不存在"),
    // 数据冲突（账号已注册、重复提交等）
    CONFLICT(409, "数据冲突"),
    // 服务器内部异常
    SERVER_ERROR(500, "服务器异常");

    // 状态码
    private final Integer code;
    // 提示信息
    private final String message;

    // 私有构造
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    // getter方法
    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

