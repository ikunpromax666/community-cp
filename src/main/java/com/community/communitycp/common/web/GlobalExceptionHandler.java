package com.community.communitycp.common.web;

import com.community.communitycp.common.core.BizException;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.core.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 把各类异常统一转成 Result 返回前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常（@Valid 校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 表单绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败：{}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 兜底：其他未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.fail(ResultCode.FAIL.getCode(), "系统异常，请稍后重试");
    }
}
