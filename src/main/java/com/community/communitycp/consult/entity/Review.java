package com.community.communitycp.consult.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问诊评价表（与订单 1:1）
 */
@Data
@TableName("review")
public class Review {

    /**
     * 雪花主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 问诊订单ID，关联consultation_order.id
     */
    private Long orderId;

    /**
     * 评价人（患者）ID，关联user.id
     */
    private Long patientId;

    /**
     * 被评价医生ID，关联user.id
     */
    private Long doctorId;

    /**
     * 评分，1~5分
     */
    private Integer score;

    /**
     * 评价文字内容，可选
     */
    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 软删除：0=未删除，1=已删除
     */
    @TableLogic
    private Integer isDeleted;
}
