package com.community.communitycp.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生列表 VO
 * <p>
 * 注意：doctorId 返回的是 doctor.user_id（= user.id），不是 doctor 表主键，
 * 因为下单时 consultation_order.doctor_id 存的就是 user.id
 */
@Data
public class DoctorListVO {

    @Schema(description = "医生ID（即 user.id，下单时用这个）", example = "3001")
    private Long doctorId;

    @Schema(description = "真实姓名", example = "赵国强")
    private String realName;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "职称", example = "主任医师")
    private String title;

    @Schema(description = "擅长领域", example = "高血压、冠心病等慢性病管理")
    private String specialty;

    @Schema(description = "图文问诊价格", example = "50.00")
    private BigDecimal consultPrice;

    @Schema(description = "评分均值", example = "4.9")
    private BigDecimal ratingAvg;

    @Schema(description = "评价数", example = "128")
    private Integer ratingCount;
}
