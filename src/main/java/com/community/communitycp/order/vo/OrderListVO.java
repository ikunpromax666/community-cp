package com.community.communitycp.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ClassName: OrderListVO
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/9 14:14
 * Version:1.0
 */
@Data
public class OrderListVO {

    @Schema(description = "订单ID（字符串，避免前端精度丢失）", example = "1234567890123456789")
    private String orderId;

    @Schema(description = "业务订单号", example = "ORD202611091414000001")
    private String orderNo;

    @Schema(description = "医生姓名", example = "李医生")
    private String doctorName;

    @Schema(description = "科室名称", example = "内科")
    private String deptName;

    @Schema(description = "订单状态", example = "1")
    private Integer orderStatus;

    @Schema(description = "订单状态名称", example = "待接诊")
    private String orderStatusName;

    @Schema(description = "订单金额", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "订单创建时间", example = "2026-11-09 14:14:00")
    private LocalDateTime createTime;
}
