package com.community.communitycp.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付结果返回
 */
@Data
public class PayVO {

    @Schema(description = "订单ID（字符串，避免前端精度丢失）")
    private String orderId;

    @Schema(description = "支付后的订单状态：1=待接诊")
    private Integer orderStatus;

    @Schema(description = "支付成功时间")
    private LocalDateTime payTime;
}
