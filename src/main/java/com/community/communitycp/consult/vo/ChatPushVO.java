package com.community.communitycp.consult.vo;

import com.community.communitycp.consult.entity.Message;
import lombok.Data;

import java.time.format.DateTimeFormatter;

/**
 * 服务端推送 / 历史消息返回的统一结构
 * <p>
 * 所有 Long 类型的 ID 一律用 String 返回，避免前端 JS 精度丢失。
 */
@Data
public class ChatPushVO {

    /** 推送时间格式：yyyy-MM-dd HH:mm:ss.SSS（与接口文档一致） */
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /** 固定值 "chat" */
    private String type;

    /** 消息ID（String 防精度丢失），撤回接口需要它 */
    private String messageId;

    private String orderId;

    /** 会话内自增序号，断线续拉的依据 */
    private Long seq;

    private String senderId;

    /** 1 患者 2 医生 */
    private Integer senderRole;

    private String senderName;

    /** 1 文本 2 图片 */
    private Integer msgType;

    private String content;

    /** 1 已送达/已读（本项目暂统一为 1） */
    private Integer msgStatus;

    private String createTime;

    public static ChatPushVO of(Message m, String senderName) {
        ChatPushVO vo = new ChatPushVO();
        vo.setType("chat");
        vo.setMessageId(String.valueOf(m.getId()));
        vo.setOrderId(String.valueOf(m.getOrderId()));
        vo.setSeq(m.getSeq());
        vo.setSenderId(String.valueOf(m.getSenderId()));
        vo.setSenderRole(m.getSenderRole());
        vo.setSenderName(senderName);
        vo.setMsgType(m.getMsgType());
        vo.setContent(m.getContent());
        vo.setMsgStatus(m.getMsgStatus());
        if (m.getCreateTime() != null) {
            vo.setCreateTime(m.getCreateTime().format(TIME_FORMATTER));
        }
        return vo;
    }
}
