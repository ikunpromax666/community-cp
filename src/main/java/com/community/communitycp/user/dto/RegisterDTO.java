/**
 * com.community.communitycp.user.dto
 */
package com.community.communitycp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册DTO
 */
@Data
public class RegisterDTO {
    /**
     * 用户名（4-20位）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 16, message = "用户名长度为4-20位")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 密码（6-20位）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 16, message = "密码长度为6-16位")
    @Schema(description = "密码", example = "123456")
    private String password;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 角色：1患者 2医生
     */
    @NotNull(message = "角色不能为空")
    @Schema(description = "角色：1=患者，2=医生", example = "1")
    private Integer role;
}
