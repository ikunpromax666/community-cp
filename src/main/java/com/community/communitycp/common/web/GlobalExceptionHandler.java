package com.community.communitycp.common.web;

import com.community.communitycp.common.core.BizException;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.core.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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
                .map(error -> error.isBindingFailure()
                        ? "参数格式错误：" + error.getField()
                        : error.getDefaultMessage())
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
                .map(error -> error.isBindingFailure()
                        ? "参数格式错误：" + error.getField()
                        : error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败：{}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * Query 参数校验异常（Controller 类上 @Validated + 参数上 @Min/@Max）
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public Result<Void> handleMethodValidationException(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 方法参数约束校验异常（Service 层 @Validated 触发）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 缺少必填 Query 参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException e) {
        String message = "缺少必要参数：" + e.getParameterName();
        log.warn("参数缺失：{}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * Query 参数类型不匹配（如把 id 传成 abc）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = "参数格式错误：" + e.getName();
        log.warn("参数类型不匹配：{}", message);
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

    /**
     * 上传文件超过大小限制
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("上传文件超过大小限制：{}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), "上传文件超过大小限制（最大 10MB）");
    }
}
