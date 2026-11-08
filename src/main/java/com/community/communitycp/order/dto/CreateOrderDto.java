package com.community.communitycp.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 下单请求参数
 */
@Data
@Schema(description = "下单请求参数")
public class CreateOrderDto {

    /**
     * 医生用户ID（即 user.id，不是 doctor.id）
     */
    @Schema(description = "医生用户ID", example = "3001")
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    /**
     * 症状描述
     */
    @Schema(description = "症状描述，可含症状、患病时长、既往病史")
    @NotBlank(message = "症状描述不能为空")
    @Size(max = 1000, message = "症状描述不能超过1000字")
    private String symptom;

    /**
     * 症状图片URL，可选
     */
    @Schema(description = "症状图片URL，可选")
    @Size(max = 255, message = "图片地址不能超过255字符")
    private String imgUrl;

    /**
     * 前端生成的请求ID，用于防重复提交
     */
    @Schema(description = "前端生成的请求ID，页面打开时生成一次，点击提交复用")
    @NotBlank(message = "请求ID不能为空")
    @Size(max = 64, message = "请求ID不能超过64字符")
    private String requestId;
}
