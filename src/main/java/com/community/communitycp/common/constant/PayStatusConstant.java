package com.community.communitycp.common.constant;

/**
 * 支付流水状态常量
 *
 * 与 payment_record.pay_status 对应：
 * 0待支付 →(支付成功) 1支付成功
 * 支付失败 → 2，全额退款 → 3
 */
public class PayStatusConstant {

    private PayStatusConstant() {
    }

    /**
     * 待支付（流水已创建但尚未完成支付）
     */
    public static final int PENDING = 0;

    /**
     * 支付成功
     */
    public static final int SUCCESS = 1;

    /**
     * 支付失败
     */
    public static final int FAILED = 2;

    /**
     * 已退款（订单关闭时全额退款）
     */
    public static final int REFUNDED = 3;

    /**
     * 状态中文名，用于支付详情展示
     */
    public static String getName(int status) {
        switch (status) {
            case PENDING:
                return "待支付";
            case SUCCESS:
                return "支付成功";
            case FAILED:
                return "支付失败";
            case REFUNDED:
                return "已退款";
            default:
                return "未知状态";
        }
    }
}
