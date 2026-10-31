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
 * 医患聊天消息表
 */
@Data
@TableName("message")
public class Message {

    /**
     * 全局雪花主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 问诊订单ID，消息归属哪一场问诊会话
     */
    private Long orderId;

    /**
     * 会话内局部消息序号，Redis INCR生成
     */
    private Long seq;

    /**
     * 客户端消息ID，前端生成，幂等防重
     */
    private String clientMsgId;

    /**
     * 发送人ID，关联user.id
     */
    private Long senderId;

    /**
     * 发送者角色：1=患者，2=医生
     */
    private Integer senderRole;

    /**
     * 消息类型：1=文本，2=图片
     */
    private Integer msgType;

    /**
     * 消息内容，文本存文字，图片存图片地址
     */
    private String content;

    /**
     * 扩展字段，JSON格式，预留图片宽高、缩略图等
     */
    private String extra;

    /**
     * 消息状态：1=正常，2=已撤回
     */
    private Integer msgStatus;

    /**
     * 消息发送时间，毫秒级
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 软删除：0=未删除，1=已删除
     */
    @TableLogic
    private Integer isDeleted;
}
