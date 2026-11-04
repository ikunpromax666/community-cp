/**
 * com.community.communitycp.doctor.dto
 */
package com.community.communitycp.doctor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorQueryDTO {

    @Schema(description = "科室ID")
    @NotNull(message = "科室ID不能为空")
    private Long deptId;

    @Schema(description = "页码（默认1）", example = "1")
    @Min(value = 1, message = "页码必须 ≥ 1")
    private Integer pageNum = 1;   // 字段直接给默认值

    @Schema(description = "每页条数（默认10，最大50）", example = "10")
    @Min(value = 1, message = "每页条数必须 ≥ 1")
    @Max(value = 50, message = "每页条数不能超过 50")
    private Integer pageSize = 10;
}
