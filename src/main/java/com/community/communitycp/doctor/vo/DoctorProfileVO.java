package com.community.communitycp.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生个人信息 VO（医生视角，仅医生本人可查看）
 * <p>
 * 与 DoctorDetailVO 的区别：
 * 1. 多返回 username、phone（手机号脱敏）
 * 2. 不返回 avatar（医生端不需要展示自己的头像）
 */
@Data
public class DoctorProfileVO {

    @Schema(description = "医生用户ID（即 user.id）")
    private Long userId;

    @Schema(description = "用户名", example = "lisi")
    private String username;

    @Schema(description = "真实姓名", example = "李医生")
    private String realName;

    @Schema(description = "手机号（脱敏）", example = "138****1234")
    private String phone;

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
