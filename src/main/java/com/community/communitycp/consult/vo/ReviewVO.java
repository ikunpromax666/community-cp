package com.community.communitycp.consult.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ClassName: ReviewVO
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/20 17:29
 * Version:1.0
 */
@Data
public class ReviewVO {
    private String id;

    private String patientName;

    private Integer score;

    private String content;

    private LocalDateTime createTime;

}
