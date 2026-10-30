package com.community.communitycp.common.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // ===== 客户端错误 4xx =====
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    // ===== 业务错误 1xxx =====
    USERNAME_EXISTS(1001, "用户名已存在"),
    USERNAME_OR_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_NOT_FOUND(1003, "用户不存在"),
    USER_NOT_AUTH(1004, "用户未实名"),

    DOCTOR_NOT_FOUND(1101, "医生不存在"),
    DOCTOR_OFFLINE(1102, "医生当前不在线"),
    DOCTOR_BUSY(1103, "医生正在问诊中，请稍后再试"),

    ORDER_NOT_FOUND(1201, "订单不存在"),
    ORDER_STATUS_ERROR(1202, "订单状态异常"),
    ORDER_NOT_PAID(1203, "订单未支付"),
    ORDER_ALREADY_PAID(1204, "订单已支付"),
    ORDER_CLOSED(1205, "订单已关闭"),
    ORDER_NOT_ACCEPTED(1206, "订单未接诊"),
    ORDER_ALREADY_FINISHED(1207, "订单已完成"),

    PAY_AMOUNT_ERROR(1301, "支付金额不一致"),
    PAY_RECORD_NOT_FOUND(1302, "支付流水不存在"),

    CONSULT_FORM_NOT_FOUND(1401, "问诊表单不存在"),
    CONSULT_NOT_IN_PROGRESS(1402, "问诊未进行中"),
    CONSULT_ALREADY_REVIEWED(1403, "该订单已评价"),

    DEPT_NOT_FOUND(1501, "科室不存在"),

    FILE_UPLOAD_ERROR(1601, "文件上传失败"),
    FILE_EMPTY(1602, "上传文件不能为空");

    private final Integer code;
    private final String message;
}
