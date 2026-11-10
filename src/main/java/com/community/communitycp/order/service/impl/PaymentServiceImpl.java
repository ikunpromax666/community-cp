package com.community.communitycp.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.communitycp.common.constant.PayStatusConstant;
import com.community.communitycp.common.core.BizException;
import com.community.communitycp.common.core.ResultCode;
import com.community.communitycp.common.web.UserContext;
import com.community.communitycp.order.entity.ConsultationOrder;
import com.community.communitycp.order.entity.PaymentRecord;
import com.community.communitycp.order.mapper.ConsultationOrderMapper;
import com.community.communitycp.order.mapper.PaymentRecordMapper;
import com.community.communitycp.order.service.PaymentService;
import com.community.communitycp.order.vo.PaymentVO;
import org.springframework.stereotype.Service;

/**
 * 支付流水服务实现
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final ConsultationOrderMapper consultationOrderMapper;

    public PaymentServiceImpl(PaymentRecordMapper paymentRecordMapper,
                              ConsultationOrderMapper consultationOrderMapper) {
        this.paymentRecordMapper = paymentRecordMapper;
        this.consultationOrderMapper = consultationOrderMapper;
    }

    @Override
    public PaymentVO getPaymentRecord(Long orderId) {
        Long patientId = UserContext.getUserId();

        // 1. 查订单并校验归属：不匹配一律按「订单不存在」处理，避免枚举他人订单
        ConsultationOrder order = consultationOrderMapper.selectById(orderId);
        if (order == null || !order.getPatientId().equals(patientId)) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 同一订单可能有多条流水（失败后重试），取最新一条
        PaymentRecord paymentRecord = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
                        .orderByDesc(PaymentRecord::getCreateTime)
                        .last("LIMIT 1"));
        if (paymentRecord == null) {
            throw new BizException(ResultCode.PAY_RECORD_NOT_FOUND);
        }

        // 3. 组装返回
        PaymentVO vo = new PaymentVO();
        vo.setOrderId(String.valueOf(orderId));
        vo.setOutTradeNo(paymentRecord.getOutTradeNo());
        vo.setAmount(paymentRecord.getAmount());
        vo.setPayChannel(paymentRecord.getPayChannel());
        vo.setPayStatus(paymentRecord.getPayStatus());
        vo.setPayStatusName(PayStatusConstant.getName(paymentRecord.getPayStatus()));
        vo.setPayTime(paymentRecord.getPayTime());
        vo.setRefundTime(paymentRecord.getRefundTime());
        return vo;
    }
}
