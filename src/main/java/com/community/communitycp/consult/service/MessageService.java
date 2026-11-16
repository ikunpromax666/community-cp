package com.community.communitycp.consult.service;

import com.community.communitycp.consult.dto.ChatMessageDTO;
import com.community.communitycp.consult.vo.ChatHistoryVO;
import com.community.communitycp.consult.vo.ChatPushVO;

/**
 * 聊天消息服务
 */
public interface MessageService {

    /**
     * 处理一条聊天消息：校验 → 幂等 → 生成 seq → 落库 → 返回推送对象
     *
     * @return 推送对象；若为重复消息返回 null，调用方应直接忽略
     */
    ChatPushVO sendMessage(Long userId, ChatMessageDTO dto);

    /**
     * 拉取历史消息（断线续拉）
     *
     * @param userId  当前登录用户
     * @param orderId 订单ID
     * @param lastSeq 已收到的最大 seq，传 0 表示从头拉
     * @param size    每次拉取条数
     */
    ChatHistoryVO getHistory(Long userId, Long orderId, Long lastSeq, Integer size);

    /**
     * 撤回消息：只把 msg_status 改为 2（已撤回），消息仍保留在库中
     *
     * @param userId    当前登录用户，必须为消息发送者
     * @param messageId 消息ID
     */
    void withdraw(Long userId, Long messageId);
}
