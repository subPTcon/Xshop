package org.michael.xshop.common.exception;

import lombok.Getter;

/**
 * 业务异常。
 * 所有"预期内"的业务失败场景（用户名重复、密码错误、地址不存在等）都应该抛这个异常，
 * 而不是返回null或者用if-else拼错误信息——统一交给全局异常处理器GlobalExceptionHandler处理，
 * 保证所有接口的错误响应格式一致。
 *
 * 使用示例：
 *   如果直接用枚举里预置的消息：
 *     throw new BusinessException(ErrorCode.USERNAME_EXISTS);
 *
 *   如果需要在预置消息基础上，带上具体上下文（比如把哪个用户名拼进去）：
 *     throw new BusinessException(ErrorCode.USERNAME_EXISTS, "用户名 " + username + " 已被注册");
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
    }
}
