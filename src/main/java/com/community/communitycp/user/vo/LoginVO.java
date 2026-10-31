package com.community.communitycp.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {

    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色：1患者 2医生 3管理员")
    private Integer role;
}
