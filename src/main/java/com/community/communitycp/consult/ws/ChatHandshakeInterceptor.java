package com.community.communitycp.consult.ws;

import com.community.communitycp.common.constant.RedisKeyConstant;
import com.community.communitycp.common.redis.RedisService;
import com.community.communitycp.common.util.JwtUtil;
import com.community.communitycp.order.entity.ConsultationOrder;
import com.community.communitycp.order.mapper.ConsultationOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * <p>
 * 浏览器原生 WebSocket 不支持自定义请求头，所以 token 与 orderId 通过 URL 参数传递：
 * ws://host:8080/api/ws/chat?token=xxx&orderId=123
 * <p>
 * 鉴权逻辑与 AuthInterceptor 保持一致：
 * 1. token 必须有效；
 * 2. Redis 中该用户的有效 token 必须与本次一致（单端登录）；
 * 3. 订单必须存在且当前用户是该订单的患者或医生。
 * <p>
 * 通过后把 userId / role / orderId / senderRole 放进 session 属性，供 Handler 使用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final ConsultationOrderMapper orderMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return reject(response, "非法请求");
        }

        String token = servletRequest.getServletRequest().getParameter("token");
        String orderIdStr = servletRequest.getServletRequest().getParameter("orderId");

        // 1. token 校验
        if (token == null || token.isBlank()) {
            return reject(response, "缺少 token");
        }
        if (!jwtUtil.validateToken(token)) {
            return reject(response, "token 无效或已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);
        if (userId == null || role == null) {
            return reject(response, "token 解析失败");
        }

        // 2. 单端登录校验
        String currentToken = redisService.get(RedisKeyConstant.loginTokenKey(userId));
        if (currentToken == null) {
            return reject(response, "登录已失效，请重新登录");
        }
        if (!currentToken.equals(token)) {
            return reject(response, "账号已在其他设备登录");
        }

        // 3. 订单归属校验
        Long orderId;
        try {
            orderId = Long.valueOf(orderIdStr);
        } catch (NumberFormatException e) {
            return reject(response, "订单ID格式错误");
        }
        ConsultationOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            return reject(response, "订单不存在");
        }
        boolean isPatient = userId.equals(order.getPatientId());
        boolean isDoctor = userId.equals(order.getDoctorId());
        if (!isPatient && !isDoctor) {
            return reject(response, "无权进入该会话");
        }

        // 4. 把身份信息挂到 session 上
        attributes.put("userId", userId);
        attributes.put("role", role);
        attributes.put("orderId", orderId);
        attributes.put("senderRole", isPatient ? 1 : 2);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private boolean reject(ServerHttpResponse response, String msg) {
        log.warn("WS 握手拒绝：{}", msg);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }
}
