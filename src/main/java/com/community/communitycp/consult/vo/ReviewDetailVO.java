package com.community.communitycp.consult.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ClassName: ReviewDetailVO
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/20 18:51
 * Version:1.0
 */
@Data
public class ReviewDetailVO {
    private String reviewId;

    private String orderId;

    private String doctorId;

    private String patientName;

    private String doctorName;

    private String content;

    private LocalDateTime createTime;

    private Integer score;

}
