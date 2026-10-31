package com.community.communitycp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新用户信息DTO（部分更新，字段均可选）
 */
@Data
public class UpProfileDTO {

    @Schema(description = "头像URL", example = "https://xxx/avatar.jpg")
    private String avatar;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
}
