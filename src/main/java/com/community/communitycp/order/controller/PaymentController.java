package com.community.communitycp.order.controller;

import com.community.communitycp.common.constant.UserRoleConstant;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.web.RequireRole;
import com.community.communitycp.order.service.PaymentService;
import com.community.communitycp.order.vo.PaymentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付流水接口
 */
@RestController
@RequestMapping("/payment")
@Tag(name = "支付接口")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 患者查看自己订单的支付流水
     */
    @Operation(summary = "查看支付流水")
    @GetMapping("/{orderId}")
    @RequireRole(UserRoleConstant.PATIENT)
    public Result<PaymentVO> getPaymentRecord(@PathVariable Long orderId) {
        return Result.success(paymentService.getPaymentRecord(orderId));
    }
}
