package com.community.communitycp.order.controller;

import com.community.communitycp.common.constant.UserRoleConstant;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.web.RequireRole;
import com.community.communitycp.order.dto.CreateOrderDto;
import com.community.communitycp.order.service.OrderService;
import com.community.communitycp.order.vo.OrderCreateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口
 */
@RestController
@Tag(name = "订单接口")
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 患者下单
     */
    @Operation(summary = "患者下单")
    @RequireRole(UserRoleConstant.PATIENT)
    @PostMapping("/create")
    public Result<OrderCreateVO> createOrder(@RequestBody @Valid CreateOrderDto dto) {
        return Result.success(orderService.createOrder(dto));
    }
}
