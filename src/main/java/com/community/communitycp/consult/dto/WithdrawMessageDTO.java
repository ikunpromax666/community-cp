package com.community.communitycp.consult.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 撤回消息
 */
@Data
public class WithdrawMessageDTO {

    @NotNull(message = "消息ID不能为空")
    private Long messageId;
}
