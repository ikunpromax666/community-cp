package com.community.communitycp.consult.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电子处方表（与订单 1:1，一单一处方，不可修改）
 */
@Data
@TableName("prescription")
public class Prescription {

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
     * 开处方医生ID，关联user.id
     */
    private Long doctorId;

    /**
     * 处方编号，业务展示用，全局唯一
     */
    private String prescriptionNo;

    /**
     * 医嘱，医生文字叮嘱
     */
    private String advice;

    /**
     * 药品信息，药品名称/规格/用量，文本存储
     */
    private String drugInfo;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 软删除：0=未删除，1=已删除
     */
    @TableLogic
    private Integer isDeleted;
}
