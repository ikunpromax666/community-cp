package com.community.communitycp.user.vo;

import com.community.communitycp.doctor.vo.DoctorInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息VO
 */
@Data
public class UserVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "手机号（脱敏）", example = "138****1234")
    private String phone;

    @Schema(description = "角色：1患者 2医生 3管理员")
    private Integer role;

    @Schema(description = "角色名称", example = "患者")
    private String roleName;

    @Schema(description = "真实姓名（脱敏）", example = "张*")
    private String realName;

    @Schema(description = "身份证号（脱敏）", example = "110101********1234")
    private String idCard;

    @Schema(description = "实名状态：0未实名 1已实名")
    private Integer isAuth;

    @Schema(description = "注册时间")
    private String createTime;

    @Schema(description = "医生信息（仅医生角色有值）")
    private DoctorInfoVO doctorInfo;
}
