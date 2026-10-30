package com.community.communitycp.common.web;

/**
 * ThreadLocal 用户上下文
 * 拦截器解析 Token 后把用户ID存进来，整个请求链路都能取
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 设置当前登录用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回 null
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清除当前线程的用户信息
     * 必须在请求结束时调用，否则线程池复用会导致数据串号
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
