package com.community.communitycp.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ClassName: PayDTO
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/8 16:46
 * Version:1.0
 */
@Data
public class OrderDTO {
    @Schema(description = "订单ID")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
