package com.community.communitycp.consult.ws;

import com.community.communitycp.common.core.BizException;
import com.community.communitycp.consult.dto.ChatMessageDTO;
import com.community.communitycp.consult.service.MessageService;
import com.community.communitycp.consult.vo.ChatPushVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天 WebSocket Handler
 * <p>
 * 职责：解析协议 → 调用 MessageService 落库 → 广播给同一会话的所有连接。
 * 业务校验、幂等、seq 生成、入库都在 Service 层（带事务）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /** orderId -> 会话内的所有连接 */
    private static final Map<Long, Set<WebSocketSession>> ROOMS = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 握手阶段已校验归属，这里把连接挂到对应房间
        Long orderId = (Long) session.getAttributes().get("orderId");
        // 用装饰器包装，避免多线程同时 sendMessage 报 TEXT_PARTIAL_WRITING
        WebSocketSession safe = new ConcurrentWebSocketSessionDecorator(session, 5000, 512 * 1024);
        ROOMS.computeIfAbsent(orderId, k -> ConcurrentHashMap.newKeySet()).add(safe);
        log.info("WS 连接建立 orderId={} session={}", orderId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = (Long) session.getAttributes().get("userId");
        Long orderId = (Long) session.getAttributes().get("orderId");

        ChatMessageDTO dto;
        try {
            dto = objectMapper.readValue(message.getPayload(), ChatMessageDTO.class);
        } catch (Exception e) {
            sendError(session, "消息格式错误");
            return;
        }

        // 客户端传的 orderId 必须与握手时的一致，防止串房间
        if (dto.getOrderId() == null || !dto.getOrderId().equals(orderId)) {
            sendError(session, "订单ID不匹配");
            return;
        }

        ChatPushVO push;
        try {
            push = messageService.sendMessage(userId, dto);
        } catch (BizException e) {
            sendError(session, e.getMessage());
            return;
        } catch (Exception e) {
            log.error("聊天消息处理失败 orderId={}", orderId, e);
            sendError(session, "消息发送失败");
            return;
        }

        // 重复消息 push 为 null，直接忽略
        if (push == null) {
            return;
        }

        broadcast(orderId, push);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long orderId = (Long) session.getAttributes().get("orderId");
        Set<WebSocketSession> room = ROOMS.get(orderId);
        if (room != null) {
            room.removeIf(s -> s.getId().equals(session.getId()));
            if (room.isEmpty()) {
                ROOMS.remove(orderId);
            }
        }
        log.info("WS 连接关闭 orderId={} session={}", orderId, session.getId());
    }

    /**
     * 广播给房间内所有人，包含发送者自己（前端靠 clientMsgId 替换本地“发送中”气泡）
     */
    private void broadcast(Long orderId, ChatPushVO push) {
        Set<WebSocketSession> room = ROOMS.get(orderId);
        if (room == null || room.isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(push);
            for (WebSocketSession s : room) {
                if (!s.isOpen()) {
                    continue;
                }
                try {
                    s.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.warn("WS 推送失败 session={}", s.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("WS 广播序列化失败 orderId={}", orderId, e);
        }
    }

    private void sendError(WebSocketSession session, String message) {
        if (!session.isOpen()) {
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "error");
            payload.put("message", message);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (Exception e) {
            log.warn("WS 错误回执发送失败 session={}", session.getId(), e);
        }
    }
}
