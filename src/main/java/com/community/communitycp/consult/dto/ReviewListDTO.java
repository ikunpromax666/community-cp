package com.community.communitycp.consult.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ClassName: ReviewListDTO
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/20 17:18
 * Version:1.0
 */
@Data
public class ReviewListDTO {

    @Schema(description = "医生用户ID（user.id，不是 doctor.id）", example = "3001")
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @Schema(description = "页码，从1开始", example = "1")
    @Min(value = 1, message = "页码从1开始")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，1~50", example = "10")
    @Min(value = 1, message = "每页至少1条")
    @Max(value = 50, message = "每页最多50条")
    private Integer pageSize = 10;

}
