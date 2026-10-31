package com.community.communitycp.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水表（模拟支付，无第三方回调，不做软删除）
 */
@Data
@TableName("payment_record")
public class PaymentRecord {

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
     * 本地业务支付单号，平台生成，用于幂等控制
     */
    private String outTradeNo;

    /**
     * 本次支付金额
     */
    private BigDecimal amount;

    /**
     * 模拟支付渠道：微信/支付宝，仅前端展示
     */
    private String payChannel;

    /**
     * 支付状态：0=待支付，1=支付成功，2=支付失败，3=已退款
     */
    private Integer payStatus;

    /**
     * 支付成功时间
     */
    private LocalDateTime payTime;

    /**
     * 退款完成时间，全额退款时赋值
     */
    private LocalDateTime refundTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
