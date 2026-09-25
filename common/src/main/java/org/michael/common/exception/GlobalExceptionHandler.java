package org.michael.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：预期内的失败场景（用户名重复、密码错误等），只记info级别日志即可，不需要打堆栈，
     * 避免正常的业务分支把日志刷成一堆"异常"，干扰真正需要关注的系统错误。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.info("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * @Valid 校验@RequestBody参数失败时抛出的异常，取第一个字段的错误提示返回，
     * 避免把所有字段的错误信息一次性堆给用户，体验更好。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ErrorCode.PARAM_INVALID.getMessage();
        log.info("参数校验失败：{}", message);
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * @Validated 校验@RequestParam/@PathVariable参数失败时抛出的异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        log.info("参数校验失败: {}", e.getMessage());
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), e.getMessage());
    }

    /**
     * 兜底异常处理：所有没有被上面几种精准捕获的异常（数据库异常、空指针、远程调用超时等），
     * 统一转成500系统错误返回，绝不能把原始异常堆栈信息暴露给调用方（可能泄露内部实现细节），
     * 但要把完整堆栈打到服务端日志里，方便排查。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR);
    }
}
