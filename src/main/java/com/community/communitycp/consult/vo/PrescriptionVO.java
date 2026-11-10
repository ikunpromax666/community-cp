package com.community.communitycp.consult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处方返回（开处方与查看处方共用）
 */
@Data
public class PrescriptionVO {

    @Schema(description = "处方ID（字符串，避免前端精度丢失）")
    private String prescriptionId;

    @Schema(description = "订单ID（字符串，避免前端精度丢失）")
    private String orderId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "处方编号")
    private String prescriptionNo;

    @Schema(description = "医嘱")
    private String advice;

    @Schema(description = "药品信息")
    private String drugInfo;

    @Schema(description = "开方时间")
    private LocalDateTime createTime;
}
