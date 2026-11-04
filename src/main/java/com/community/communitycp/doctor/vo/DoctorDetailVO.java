package com.community.communitycp.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生详情 VO（患者视角，公开接口返回）
 * <p>
 * 注意：userId 是 user 表主键，也是下单时用的医生ID，不是 doctor 表主键
 */
@Data
public class DoctorDetailVO {

    @Schema(description = "医生用户ID（即 user.id，下单时用这个）", example = "1234567890123456789")
    private Long userId;

    @Schema(description = "真实姓名", example = "李医生")
    private String realName;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "科室ID")
    private Long deptId;

    @Schema(description = "科室名称", example = "内科")
    private String deptName;

    @Schema(description = "职称", example = "主任医师")
    private String title;

    @Schema(description = "擅长领域", example = "擅长心血管疾病")
    private String specialty;

    @Schema(description = "图文问诊价格", example = "50.00")
    private BigDecimal consultPrice;

    @Schema(description = "在线状态：0=离线，1=在线")
    private Integer onlineStatus;

    @Schema(description = "问诊状态：0=空闲，1=问诊中")
    private Integer consultStatus;

    @Schema(description = "评分均值", example = "4.8")
    private BigDecimal ratingAvg;

    @Schema(description = "评价数", example = "120")
    private Integer ratingCount;
}
