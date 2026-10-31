/**
 * com.community.communitycp.doctor.entity
 */
package com.community.communitycp.doctor.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("doctor")
@Data
public class Doctor {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     *  关联user.id
     */
    private Long userId;

    /**
     * 归属科室Id
     */
    private Long deptId;

    /**
     * 医生职称，如主治医师、主任医师
     */
    private String title;

    /**
     医生擅长领域，医生详情页展示
     */
    private String specialty;

    /**
     * 图文问诊价格，下单时取此
     */
    private BigDecimal consultPrice;

    /**
     * 在线/接诊开关：0=离线不接单，1=在
     */
    private Integer onlineStatus;

    /**
     * 问诊状态：0=空闲可接诊，1=问诊中
     */
    private Integer consultStatus;

    /**
     * 评分均值
     */
    private BigDecimal ratingAvg;

    /**
     * 评价数
     */
    private Integer ratingCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 软删除标志：0=未删除，1=已删除
     */
    @TableLogic
    private Integer isDeleted;

}
