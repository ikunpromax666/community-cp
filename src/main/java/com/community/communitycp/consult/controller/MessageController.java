package com.community.communitycp.consult.controller;

import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.web.RequireRole;
import com.community.communitycp.common.web.UserContext;
import com.community.communitycp.common.constant.UserRoleConstant;
import com.community.communitycp.consult.dto.WithdrawMessageDTO;
import com.community.communitycp.consult.service.MessageService;
import com.community.communitycp.consult.vo.ChatHistoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天消息接口
 */
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 历史消息 / 断线续拉
     * <p>
     * 客户端记录已收到的最大 seq，传入 lastSeq，服务端返回 seq > lastSeq 的消息。
     * 首次进入传 lastSeq=0；如果想取“最近 N 条”，前端可先取一次大的 lastSeq（如 Long.MAX_VALUE）
     * 再倒序，但本接口统一按升序返回，便于断线续拉。
     *
     * @param orderId 订单ID
     * @param lastSeq 已收到的最大 seq
     * @param size    每次条数，默认 20，最大 100
     */
    @GetMapping("/history")
    @RequireRole({UserRoleConstant.PATIENT, UserRoleConstant.DOCTOR})
    public Result<ChatHistoryVO> history(@RequestParam Long orderId,
                                         @RequestParam(defaultValue = "0") Long lastSeq,
                                         @RequestParam(defaultValue = "20") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.getHistory(userId, orderId, lastSeq, size));
    }

    /**
     * 撤回消息：仅发送者本人，只把 msg_status 改为 2，消息保留在库中
     */
    @PutMapping("/withdraw")
    @RequireRole({UserRoleConstant.PATIENT, UserRoleConstant.DOCTOR})
    public Result<Void> withdraw(@Valid @RequestBody WithdrawMessageDTO dto) {
        messageService.withdraw(UserContext.getUserId(), dto.getMessageId());
        return Result.success();
    }
}
