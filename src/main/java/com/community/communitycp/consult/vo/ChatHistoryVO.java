package com.community.communitycp.consult.vo;

import lombok.Data;

import java.util.List;

/**
 * 历史消息响应
 */
@Data
public class ChatHistoryVO {

    /** 本次返回的消息列表（按 seq 升序） */
    private List<ChatPushVO> list;

    /** 是否还有更早的消息未返回 */
    private Boolean hasMore;

    /** 本次返回的最大 seq，客户端记录下来用于续拉 */
    private Long lastSeq;
}
