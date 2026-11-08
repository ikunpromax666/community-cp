package com.community.communitycp.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 下单响应
 */
@Data
@Schema(description = "下单结果")
public class OrderCreateVO {

    /**
     * 雪花 ID 为 19 位，超出 JS 安全整数范围，用字符串返回避免前端精度丢失
     */
    @Schema(description = "订单ID", example = "1234567890123456789")
    private String orderId;

    @Schema(description = "订单号", example = "ORD202611071030001234")
    private String orderNo;

    @Schema(description = "问诊金额（下单时医生价格快照）")
    private BigDecimal amount;

    @Schema(description = "订单状态：0=待支付")
    private Integer orderStatus;
}
