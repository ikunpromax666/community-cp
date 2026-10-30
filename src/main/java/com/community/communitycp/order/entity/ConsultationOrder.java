package com.community.communitycp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 问诊订单表（核心主表）
 */
@Data
@TableName("consultation_order")
public class ConsultationOrder {

    /**
     * 雪花主键ID，订单唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务订单号，给用户看的单号
     */
    private String orderNo;

    /**
     * 患者ID，关联user.id
     */
    private Long patientId;

    /**
     * 医生ID，关联user.id
     */
    private Long doctorId;

    /**
     * 科室ID，关联department.id
     */
    private Long deptId;

    /**
     * 订单状态：0待支付,1待接诊,2问诊中,3已完成,4超时关闭,5用户放弃关闭,6拒诊/接诊超时退款关闭,7用户取消退款关闭
     */
    private Integer orderStatus;

    /**
     * 问诊费用，下单时取医生价格快照写入
     */
    private BigDecimal amount;

    /**
     * 支付成功时间
     */
    private LocalDateTime payTime;

    /**
     * 医生接诊时间
     */
    private LocalDateTime acceptTime;

    /**
     * 问诊完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 订单关闭时间
     */
    private LocalDateTime closeTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 软删除：0=正常，1=删除
     */
    @TableLogic
    private Integer isDeleted;
}
