package com.community.communitycp.doctor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ClassName: OnlineStatusDTO
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/4 20:35
 * Version:1.0
 */
@Data
public class OnlineStatusDTO {
    @Schema(description = "在线状态：0=离线，1=在线")
    @Min(value = 0, message = "在线状态必须为0或1")
    @Max(value = 1, message = "在线状态必须为0或1")
    @NotNull(message = "在线状态不能为空")
    private Integer onlineStatus;
}
