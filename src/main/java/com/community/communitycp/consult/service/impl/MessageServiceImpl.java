package com.community.communitycp.consult.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.communitycp.common.constant.OrderStatusConstant;
import com.community.communitycp.common.constant.RedisKeyConstant;
import com.community.communitycp.common.core.BizException;
import com.community.communitycp.common.core.ResultCode;
import com.community.communitycp.common.redis.RedisService;
import com.community.communitycp.consult.dto.ChatMessageDTO;
import com.community.communitycp.consult.entity.Message;
import com.community.communitycp.consult.mapper.MessageMapper;
import com.community.communitycp.consult.service.MessageService;
import com.community.communitycp.consult.vo.ChatHistoryVO;
import com.community.communitycp.consult.vo.ChatPushVO;
import com.community.communitycp.order.entity.ConsultationOrder;
import com.community.communitycp.order.mapper.ConsultationOrderMapper;
import com.community.communitycp.user.entity.User;
import com.community.communitycp.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    /** msg_status：1=正常，2=已撤回 */
    private static final int MESSAGE_STATUS_WITHDRAWN = 2;

    private final MessageMapper messageMapper;
    private final ConsultationOrderMapper orderMapper;
    private final UserMapper userMapper;
    private final RedisService redisService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatPushVO sendMessage(Long userId, ChatMessageDTO dto) {
        Long orderId = dto.getOrderId();

        // 1. 订单必须存在且当前用户是该订单的患者或医生
        ConsultationOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        boolean isPatient = userId.equals(order.getPatientId());
        boolean isDoctor = userId.equals(order.getDoctorId());
        if (!isPatient && !isDoctor) {
            throw new BizException(ResultCode.FORBIDDEN);
        }

        // 2. 只有「问诊中」状态允许聊天
        if (order.getOrderStatus() == null || order.getOrderStatus() != OrderStatusConstant.IN_PROGRESS) {
            throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 3. 幂等：同一 (orderId, clientMsgId) 只入库一次
        boolean first = redisService.setIfAbsent(
                RedisKeyConstant.chatIdemKey(orderId, dto.getClientMsgId()), "1", 1, TimeUnit.HOURS);
        if (!first) {
            return null;
        }

        // 4. 会话内自增 seq（7 天过期，避免永久堆积）
        long seq = redisService.incr(RedisKeyConstant.chatSeqKey(orderId), 7, TimeUnit.DAYS);

        // 5. 落库
        Message entity = new Message();
        entity.setOrderId(orderId);
        entity.setSeq(seq);
        entity.setClientMsgId(dto.getClientMsgId());
        entity.setSenderId(userId);
        entity.setSenderRole(isPatient ? 1 : 2);
        entity.setMsgType(dto.getMsgType());
        entity.setContent(dto.getContent());
        entity.setMsgStatus(1);
        messageMapper.insert(entity);

        // 6. 组装推送对象（ID 全部转 String）
        User sender = userMapper.selectById(userId);
        String senderName = sender != null ? sender.getRealName() : null;
        return ChatPushVO.of(entity, senderName);
    }

    @Override
    public ChatHistoryVO getHistory(Long userId, Long orderId, Long lastSeq, Integer size) {
        if (lastSeq == null) {
            lastSeq = 0L;
        }
        if (size == null || size <= 0) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }

        // 1. 归属校验
        ConsultationOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!userId.equals(order.getPatientId()) && !userId.equals(order.getDoctorId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }

        // 2. 比 lastSeq 大的消息，按 seq 升序；多查一条判断 hasMore
        List<Message> list = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getOrderId, orderId)
                .gt(Message::getSeq, lastSeq)
                .orderByAsc(Message::getSeq)
                .last("LIMIT " + (size + 1)));

        boolean hasMore = list.size() > size;
        if (hasMore) {
            list = list.subList(0, size);
        }

        // 3. 组装返回
        List<ChatPushVO> voList = new ArrayList<>(list.size());
        Long maxSeq = lastSeq;
        for (Message m : list) {
            User sender = userMapper.selectById(m.getSenderId());
            String senderName = sender != null ? sender.getRealName() : null;
            voList.add(ChatPushVO.of(m, senderName));
            maxSeq = m.getSeq();
        }

        ChatHistoryVO vo = new ChatHistoryVO();
        vo.setList(voList);
        vo.setHasMore(hasMore);
        vo.setLastSeq(maxSeq);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BizException(ResultCode.MESSAGE_NOT_FOUND);
        }
        // 只有发送者本人可以撤回
        if (!userId.equals(message.getSenderId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        // 已撤回直接返回，保证重复调用幂等
        if (message.getMsgStatus() != null && message.getMsgStatus() == MESSAGE_STATUS_WITHDRAWN) {
            return;
        }

        // 条件更新：msg_status != 2 才更新，靠影响行数兜住并发重复撤回
        int rows = messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getId, messageId)
                .eq(Message::getSenderId, userId)
                .ne(Message::getMsgStatus, MESSAGE_STATUS_WITHDRAWN)
                .set(Message::getMsgStatus, MESSAGE_STATUS_WITHDRAWN));
        if (rows == 0) {
            log.debug("消息已被撤回 messageId={}", messageId);
        }
    }
}
