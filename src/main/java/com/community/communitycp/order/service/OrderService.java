package com.community.communitycp.order.service;

import com.community.communitycp.order.dto.CreateOrderDto;
import com.community.communitycp.order.vo.OrderCreateVO;

/**
 * 订单服务
 */
public interface OrderService {

    /**
     * 患者下单：同一事务内创建订单和问诊表单
     *
     * @param dto 下单参数
     * @return 订单ID、订单号、金额、状态
     */
    OrderCreateVO createOrder(CreateOrderDto dto);
}
