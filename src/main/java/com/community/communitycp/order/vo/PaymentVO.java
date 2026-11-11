package com.community.communitycp.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水返回
 */
@Data
public class PaymentVO {

    @Schema(description = "订单ID（字符串，避免前端精度丢失）")
    private String orderId;

    @Schema(description = "本地支付单号")
    private String outTradeNo;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "支付状态：0=待支付，1=支付成功，2=支付失败，3=已退款")
    private Integer payStatus;

    @Schema(description = "支付状态中文名")
    private String payStatusName;

    @Schema(description = "支付成功时间")
    private LocalDateTime payTime;

    @Schema(description = "退款完成时间，未退款时为 null")
    private LocalDateTime refundTime;
}
