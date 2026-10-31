package com.community.communitycp.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生信息VO（嵌套在 UserVO 中，仅 role=2 时有值）
 */
@Data
public class DoctorInfoVO {

    @Schema(description = "科室ID")
    private Long deptId;

    @Schema(description = "职称", example = "主任医师")
    private String title;

    @Schema(description = "擅长领域", example = "心血管疾病")
    private String specialty;

    @Schema(description = "问诊价格", example = "50.00")
    private BigDecimal consultPrice;

    @Schema(description = "在线状态：0离线 1在线")
    private Integer onlineStatus;

    @Schema(description = "问诊状态：0空闲 1问诊中")
    private Integer consultStatus;

    @Schema(description = "评分均值", example = "4.8")
    private BigDecimal ratingAvg;

    @Schema(description = "评价数", example = "120")
    private Integer ratingCount;
}
