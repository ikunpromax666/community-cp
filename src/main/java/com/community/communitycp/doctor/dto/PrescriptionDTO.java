package com.community.communitycp.doctor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ClassName: PrescriptionDTO
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/10 18:08
 * Version:1.0
 */
@Data
public class PrescriptionDTO {
    @Schema(description = "订单ID")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "处方")
    @Size(max = 200, message = "处方不能超过200个字符")
    @NotBlank(message = "处方不能为空不能为空")
    private String advice;

    @Schema(description = "药品信息")
    @NotBlank(message = "药品信息不能为空不能为空")
    @Size(max = 200, message = "药品信息不能超过200个字符")
    private String drugInfo;
}
