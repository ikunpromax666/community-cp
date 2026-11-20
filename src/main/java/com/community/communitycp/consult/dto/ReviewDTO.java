package com.community.communitycp.consult.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ClassName: ReviewDTO
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/16 17:31
 * Version:1.0
 */
@Data
public class ReviewDTO {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 评分，1~5 分 */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer score;

    /** 评价内容，可选 */
    @Size(max = 200, message = "评价内容不能超过200个字符")
    private String content;
}
