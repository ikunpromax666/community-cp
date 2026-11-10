package com.community.communitycp.order.service;

import com.community.communitycp.order.vo.PaymentVO;

/**
 * 支付流水服务
 */
public interface PaymentService {

    /**
     * 查询订单的支付流水（取最新一条）
     */
    PaymentVO getPaymentRecord(Long orderId);
}
