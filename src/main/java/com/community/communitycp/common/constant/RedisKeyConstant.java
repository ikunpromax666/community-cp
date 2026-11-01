package com.community.communitycp.common.constant;

/**
 * Redis Key 常量
 */
public class RedisKeyConstant {

    /**
     * 登录 Token：login:token:{userId} -> 当前有效 token
     * <p>
     * 单端登录：同一 userId 只保留最后一次登录的 token，新登录覆盖旧值
     */
    public static final String LOGIN_TOKEN = "login:token:";

    /**
     * 拼装登录 Token 的 Key
     */
    public static String loginTokenKey(Long userId) {
        return LOGIN_TOKEN + userId;
    }
}
