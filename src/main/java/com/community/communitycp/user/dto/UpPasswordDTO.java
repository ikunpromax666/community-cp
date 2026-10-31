package com.community.communitycp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新密码DTO
 */
@Data
public class UpPasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    @Schema(description = "旧密码", example = "123456")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    @Schema(description = "新密码", example = "654321")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    @Schema(description = "确认新密码", example = "654321")
    private String confirmPassword;
}
