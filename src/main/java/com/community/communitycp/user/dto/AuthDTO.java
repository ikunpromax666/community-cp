package com.community.communitycp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 实名认证DTO
 */
@Data
public class AuthDTO {

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 10, message = "真实姓名长度必须在2到10之间")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(message = "身份证号格式错误", regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$")
    @Size(min = 18, max = 18, message = "身份证号长度必须为18位")
    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;
}
