package com.community.communitycp.common.constant;

/**
 * Redis Key 常量
 */
public class RedisKeyConstant {

    /**
     * 登录 Token：login:user:{userId} -> 当前有效 token
     * <p>
     * 单端登录：同一 userId 只保留最后一次登录的 token，新登录覆盖旧值
     */
    public static final String LOGIN_TOKEN = "login:user:";

    /**
     * 部门列表：dept:list -> 部门列表
     */
    public static final String DEPT_LIST = "dept:list";

    /**
     * 拼装登录 Token 的 Key
     */
    public static String loginTokenKey(Long userId) {
        return LOGIN_TOKEN + userId;
    }

    public static String deptListKey() {
        return DEPT_LIST;
    }

    /**
     * 医生列表分页缓存前缀：doctor:dept:{deptId}:p{pageNum}:s{pageSize}
     */
    public static final String DOCTOR_DEPT_PAGE = "doctor:dept:";

    /**
     * 医生列表互斥锁前缀：lock:doctor:dept:{deptId}
     */
    public static final String DOCTOR_DEPT_LOCK = "lock:doctor:dept:";

    /**
     * 按科室分页缓存的 Key
     */
    public static String doctorDeptPageKey(Long deptId, long pageNum, long pageSize) {
        return DOCTOR_DEPT_PAGE + deptId + ":p" + pageNum + ":s" + pageSize;
    }

    /**
     * 按科室批量失效缓存时用的前缀
     */
    public static String doctorDeptPrefix(Long deptId) {
        return DOCTOR_DEPT_PAGE + deptId + ":";
    }

    /**
     * 按科室加锁的 Key
     */
    public static String doctorDeptLockKey(Long deptId) {
        return DOCTOR_DEPT_LOCK + deptId;
    }

    /**
     * 医生详情缓存前缀：doctor:detail:{doctorId}
     */
    public static final String DOCTOR_DETAIL = "doctor:detail:";

    /**
     * 医生详情互斥锁前缀：lock:doctor:detail:{doctorId}
     */
    public static final String DOCTOR_DETAIL_LOCK = "lock:doctor:detail:";

    /**
     * 医生详情缓存 Key（doctorId 与详情接口入参一致，即 user.id）
     */
    public static String doctorDetailKey(Long doctorId) {
        return DOCTOR_DETAIL + doctorId;
    }

    /**
     * 医生详情加锁的 Key
     */
    public static String doctorDetailLockKey(Long doctorId) {
        return DOCTOR_DETAIL_LOCK + doctorId;
    }

    /**
     * 下单幂等前缀：idempotent:order:{userId}:{requestId}
     */
    public static final String IDEMPOTENT_ORDER = "idempotent:order:";

    /**
     * 下单幂等 Key：同一用户同一 requestId 只允许成功提交一次
     */
    public static String idempotentOrderKey(Long userId, String requestId) {
        return IDEMPOTENT_ORDER + userId + ":" + requestId;
    }

    /**
     * 消息会话内自增序号：chat:seq:{orderId}
     */
    public static final String CHAT_SEQ = "chat:seq:";

    /**
     * 消息幂等前缀：chat:idem:{orderId}:{clientMsgId}
     */
    public static final String CHAT_IDEM = "chat:idem:";

    /**
     * 消息序号 Key：chat:seq:{orderId}，7 天过期
     */
    public static String chatSeqKey(Long orderId) {
        return CHAT_SEQ + orderId;
    }

    /**
     * 消息幂等 Key：chat:idem:{orderId}:{clientMsgId}，1 小时过期
     */
    public static String chatIdemKey(Long orderId, String clientMsgId) {
        return CHAT_IDEM + orderId + ":" + clientMsgId;
    }
}
