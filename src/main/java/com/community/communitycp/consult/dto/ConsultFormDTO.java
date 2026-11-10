/**
 * com.community.communitycp.consult.dto
 */
package com.community.communitycp.consult.dto;

    import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsultFormDTO {
    @Schema(description = "症状描述")
    @NotBlank(message = "症状描述不能为空")
    @Size(max = 1000, message = "症状描述不能超过1000个字符")
    private String symptom;

    @Schema(description = "图片URL")
    private String imgUrl;
}
