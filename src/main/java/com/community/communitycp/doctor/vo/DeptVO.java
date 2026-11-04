package com.community.communitycp.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 科室列表 VO：只返回前端展示所需字段
 * <p>
 * 不返回 status（查询时已固定启用）、sort（顺序已在结果中体现）、
 * createTime/updateTime/isDeleted（内部字段，不应暴露）
 */
@Data
public class DeptVO {

    @Schema(description = "科室ID", example = "1001")
    private Long id;

    @Schema(description = "科室名称", example = "内科")
    private String name;

    @Schema(description = "科室简介", example = "诊治呼吸系统、消化系统等内科常见疾病")
    private String intro;
}
