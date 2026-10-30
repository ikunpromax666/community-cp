package com.community.communitycp.common.core;

import lombok.Getter;

/**
 * 业务异常
 * 抛出后由 GlobalExceptionHandler 捕获，转成 Result 返回前端
 */
@Getter
public class BizException extends RuntimeException {

    /**
     * 错误状态码
     */
    private final Integer code;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }



    public BizException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }
}
