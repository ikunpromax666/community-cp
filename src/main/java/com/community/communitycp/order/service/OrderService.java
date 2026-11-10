package com.community.communitycp.order.service;

import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.order.dto.CreateOrderDto;
import com.community.communitycp.order.dto.OrderDTO;
import com.community.communitycp.order.dto.OrderListDTO;
import com.community.communitycp.order.vo.DetailOrderVO;
import com.community.communitycp.order.vo.OrderCreateVO;
import com.community.communitycp.order.vo.OrderListVO;
import com.community.communitycp.order.vo.PayVO;
import jakarta.validation.Valid;

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

    /**
     * 患者支付：更新订单状态为已支付
     * @param dto 支付参数，包含订单ID和支付金额
     * @return 支付成功后的订单状态、支付时间等信息
     */
    PayVO payOrder( OrderDTO dto);

    /**
     * 患者取消订单：更新订单状态为已取消
     * @param dto 取消订单参数，包含订单ID
     */
    void cancelOrder(OrderDTO dto);

    /**
     * 患者查看订单详情：仅订单所属患者本人可访问
     * @param orderId 订单ID
     * @return 订单信息 + 问诊表单 + 处方
     */
    DetailOrderVO getOrderDetail(Long orderId);

    /**
     * 患者查看我的订单列表：分页 + 可选状态过滤
     * @param dto 分页与状态参数
     * @return 当前页订单摘要 + 分页元信息
     */
    PageResult<OrderListVO> getMyOrderList(OrderListDTO dto);
}
