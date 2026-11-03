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
}
