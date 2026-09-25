package org.michael.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(20000, "success"),

    // 400xx 参数校验类
    PARAM_INVALID(40000, "请求参数不合法"),
    PARAM_MISSING(40001, "缺少必填参数"),

    // 401xx 认证类
    UNAUTHORIZED(40100, "请先登录"),
    TOKEN_EXPIRED(40101, "登录状态已过期，请重新登录"),
    TOKEN_INVALID(40102, "无效的登录凭证"),
    LOGIN_FAILED(40103, "用户名或密码错误"),
    ACCOUNT_DISABLED(40104, "账号已被禁用，请联系客服"),
    LOGIN_TOO_MANY_ATTEMPTS(40105, "登录失败次数过多，请稍后重试"),

    // 403xx 权限类
    FORBIDDEN(40300, "无权限执行该操作"),
    ADDRESS_NOT_OWNED(40301, "无权限操作他人的收货地址"),

    // 404xx 资源不存在类
    USER_NOT_FOUND(40400, "用户不存在"),
    ADDRESS_NOT_FOUND(40401, "收货地址不存在"),

    // 409xx 业务冲突类
    USERNAME_EXISTS(40900, "该用户名已被注册"),
    PHONE_EXISTS(40901, "该手机号已被注册"),
    EMAIL_EXISTS(40902, "该邮箱已被注册"),
    OLD_PASSWORD_INCORRECT(40903, "原密码不正确"),
    REGISTER_CONFLICT(40904, "注册失败，用户名或手机号已被占用，请重试"),

    // 500xx 系统错误类
    SYSTEM_ERROR(50000, "系统繁忙，请稍后重试"),
    REMOTE_SERVICE_ERROR(50001, "依赖的服务暂时不可用"),
    DATABASE_ERROR(50002, "数据操作失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
