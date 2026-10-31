package com.community.communitycp.consult.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问诊表单表（与订单 1:1）
 */
@Data
@TableName("consult_form")
public class ConsultForm {

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
     * 症状描述，含症状、患病时长、既往病史
     */
    private String symptom;

    /**
     * 症状图片地址，可选
     */
    private String imgUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 软删除：0=未删除，1=已删除
     */
    @TableLogic
    private Integer isDeleted;
}
