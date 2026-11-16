package com.community.communitycp.consult.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 客户端发送的聊天消息
 */
@Data
public class ChatMessageDTO {

    /** 固定值 "chat"，预留扩展用（目前可不校验） */
    private String type;

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 客户端消息ID，用于幂等（同一 orderId 下唯一） */
    @NotBlank(message = "客户端消息ID不能为空")
    @Size(max = 64, message = "客户端消息ID不能超过64个字符")
    private String clientMsgId;

    /** 1 文本 2 图片 */
    @NotNull(message = "消息类型不能为空")
    private Integer msgType;

    /** 文本内容 或 图片 URL */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000个字符")
    private String content;
}
