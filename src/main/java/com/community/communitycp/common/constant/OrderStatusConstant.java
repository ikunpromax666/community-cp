package com.community.communitycp.common.constant;

/**
 * 订单状态常量
 *
 * 正常流转：0待支付 →(支付) 1待接诊 →(接诊) 2问诊中 →(开处方) 3已完成
 * 关闭分支：0→4/5，1→6/7，2→8
 */
public class OrderStatusConstant {

    private OrderStatusConstant() {
    }

    /**
     * 待支付
     */
    public static final int PENDING_PAY = 0;

    /**
     * 待接诊
     */
    public static final int PENDING_ACCEPT = 1;

    /**
     * 问诊中
     */
    public static final int IN_PROGRESS = 2;

    /**
     * 已完成
     */
    public static final int FINISHED = 3;

    /**
     * 待支付超时关闭（无退款）
     */
    public static final int TIMEOUT_CLOSED = 4;

    /**
     * 用户放弃关闭（支付前取消，无退款）
     */
    public static final int USER_ABANDONED = 5;

    /**
     * 拒诊/接诊超时关闭（全额退款，不重新派单）
     */
    public static final int REJECTED_CLOSED = 6;

    /**
     * 用户取消关闭（支付后待接诊时取消，全额退款）
     */
    public static final int USER_CANCELLED = 7;

    /**
     * 问诊超时关闭（接诊后超24小时未开处方，全额退款并释放医生）
     */
    public static final int CONSULT_TIMEOUT_CLOSED = 8;

    /**
     * 是否为终态：终态订单不可再发生任何状态变更
     */
    public static boolean isFinalStatus(int status) {
        return status == FINISHED
                || status == TIMEOUT_CLOSED
                || status == USER_ABANDONED
                || status == REJECTED_CLOSED
                || status == USER_CANCELLED
                || status == CONSULT_TIMEOUT_CLOSED;
    }

    /**
     * 状态中文名，用于订单详情展示
     */
    public static String getName(int status) {
        switch (status) {
            case PENDING_PAY:
                return "待支付";
            case PENDING_ACCEPT:
                return "待接诊";
            case IN_PROGRESS:
                return "问诊中";
            case FINISHED:
                return "已完成";
            case TIMEOUT_CLOSED:
                return "超时关闭";
            case USER_ABANDONED:
                return "用户放弃关闭";
            case REJECTED_CLOSED:
                return "拒诊/接诊超时关闭";
            case USER_CANCELLED:
                return "用户取消关闭";
            case CONSULT_TIMEOUT_CLOSED:
                return "问诊超时关闭";
            default:
                return "未知状态";
        }
    }
}
