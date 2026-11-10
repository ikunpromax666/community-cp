package com.community.communitycp.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * ClassName: OrderListDTO
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/9 14:51
 * Version:1.0
 */
@Data
public class OrderListDTO {
    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "页码（默认1）", example = "1")
    @Min(value = 1, message = "页码必须 ≥ 1")
    private Integer pageNum = 1;   // 字段直接给默认值

    @Schema(description = "每页条数（默认10，最大50）", example = "10")
    @Min(value = 1, message = "每页条数必须 ≥ 1")
    @Max(value = 50, message = "每页条数不能超过 50")
    private Integer pageSize = 10;
}
