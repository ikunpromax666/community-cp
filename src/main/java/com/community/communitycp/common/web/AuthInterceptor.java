package com.community.communitycp.common.web;

import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.core.ResultCode;
import com.community.communitycp.common.constant.RedisKeyConstant;
import com.community.communitycp.common.redis.RedisService;
import com.community.communitycp.common.util.JwtUtil;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器
 * 1. 从请求头提取 Token 并校验
 * 2. 解析出 userId 和 role 存入 UserContext
 * 3. 校验接口上的 @RequireRole 注解
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    @Value("${community.jwt.header}")
    private String headerName;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非方法请求（静态资源等）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 从请求头取 token
        String header = request.getHeader(headerName);
        String token = jwtUtil.resolveToken(header);

        // token 不存在或无效
        if (token == null || !jwtUtil.validateToken(token)) {
            writeError(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        // 解析用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);
        if (userId == null || role == null) {
            writeError(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        // 单端登录校验：Redis 中该用户的有效 token 必须与本次请求的 token 一致
        String currentToken = redisService.get(RedisKeyConstant.loginTokenKey(userId));
        if (currentToken == null) {
            // 已登出或 Redis 中无记录
            writeError(response, ResultCode.UNAUTHORIZED);
            return false;
        }
        if (!currentToken.equals(token)) {
            // 账号已在其他设备登录，旧 token 已被覆盖
            writeError(response, ResultCode.TOKEN_KICKED_OUT);
            return false;
        }

        // 存入上下文
        UserContext.setUserId(userId);

        // 角色校验：方法上有 @RequireRole 则校验
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole != null) {
            int[] allowedRoles = requireRole.value();
            boolean allowed = false;
            for (int r : allowedRoles) {
                if (r == role) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                writeError(response, ResultCode.FORBIDDEN);
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束必须清除 ThreadLocal，防止线程池复用导致串号
        UserContext.clear();
    }

    /**
     * 写入错误响应
     */
    private void writeError(HttpServletResponse response, ResultCode resultCode) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<Void> result = Result.fail(resultCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
