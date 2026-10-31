package com.community.communitycp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ClassName: LoginDTO
 * Description:
 * Author: SuperXia
 * Datetime :2026/10/31 17:25
 * Version:1.0
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度为4-20位")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    private String password;
}
