package com.community.communitycp.order.vo;

import com.community.communitycp.consult.entity.ConsultForm;
import com.community.communitycp.consult.entity.Prescription;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ClassName: DetailOrderVO
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/9 12:01
 * Version:1.0
 */
@Data
public class DetailOrderVO {

    @Schema(description = "订单ID（字符串，避免前端精度丢失）")
    private String orderId;

    @Schema(description = "业务订单号")
    private String orderNo;

    @Schema(description = "患者ID（字符串，避免前端精度丢失）")
    private String patientId;

    @Schema(description = "患者姓名")
    private String patientName;

    @Schema(description = "医生ID（字符串，避免前端精度丢失）")
    private String doctorId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "科室ID（字符串，避免前端精度丢失）")
    private String deptId;

    @Schema(description = "科室名称")
    private String deptName;

    @Schema(description = "订单状态：0待支付,1待接诊,2问诊中,3已完成,4-8关闭类")
    private Integer orderStatus;

    @Schema(description = "订单状态名称")
    private String orderStatusName;

    @Schema(description = "问诊费用（下单时的价格快照）")
    private BigDecimal amount;

    @Schema(description = "支付成功时间")
    private LocalDateTime payTime;

    @Schema(description = "医生接诊时间")
    private LocalDateTime acceptTime;

    @Schema(description = "问诊完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "订单关闭时间")
    private LocalDateTime closeTime;

    @Schema(description = "下单时间")
    private LocalDateTime createTime;

    @Schema(description = "问诊表单：症状描述与症状图片")
    private ConsultForm consultForm;

    @Schema(description = "处方信息，未开处方时为 null")
    private Prescription prescription;
}
