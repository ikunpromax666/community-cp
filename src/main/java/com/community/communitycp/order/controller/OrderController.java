package com.community.communitycp.order.controller;

import com.community.communitycp.common.constant.UserRoleConstant;
import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.web.RequireRole;
import com.community.communitycp.order.dto.CreateOrderDto;
import com.community.communitycp.order.dto.OrderDTO;
import com.community.communitycp.order.dto.OrderListDTO;
import com.community.communitycp.order.service.OrderService;
import com.community.communitycp.order.vo.DetailOrderVO;
import com.community.communitycp.order.vo.OrderCreateVO;
import com.community.communitycp.order.vo.OrderListVO;
import com.community.communitycp.order.vo.PayVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 患者支付
     */
    @Operation(summary = "患者支付")
    @PostMapping("/pay")
    @RequireRole(UserRoleConstant.PATIENT)
    public Result<PayVO> payOrder(@RequestBody @Valid OrderDTO dto) {
        return Result.success(orderService.payOrder(dto));
    }

    /**
     * 患者取消订单
     */
    @Operation(summary = "患者取消订单")
    @PostMapping("/cancel")
    @RequireRole(UserRoleConstant.PATIENT)
    public Result<Void> cancelOrder(@RequestBody @Valid OrderDTO dto) {
        orderService.cancelOrder(dto);
        return Result.success();
    }

    /**
     * 患者查看订单详情（仅订单所属患者本人）
     */
    @Operation(summary = "患者查看订单详情")
    @GetMapping("/detail/{id}")
    @RequireRole(UserRoleConstant.PATIENT)
    public Result<DetailOrderVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    /**
     * 患者查看我的订单列表（分页，可传 status 过滤）
     */
    @Operation(summary = "患者查看我的订单列表")
    @GetMapping("/my")
    @RequireRole(UserRoleConstant.PATIENT)
    public Result<PageResult<OrderListVO>> getMyOrderList(@Valid OrderListDTO dto) {
        return Result.success(orderService.getMyOrderList(dto));
    }

}
