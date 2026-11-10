/**
 * com.community.communitycp.consult.service.impl
 */
package com.community.communitycp.consult.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.communitycp.common.constant.OrderStatusConstant;
import com.community.communitycp.common.core.BizException;
import com.community.communitycp.common.core.ResultCode;
import com.community.communitycp.common.web.UserContext;
import com.community.communitycp.consult.dto.ConsultFormDTO;
import com.community.communitycp.consult.entity.ConsultForm;
import com.community.communitycp.consult.mapper.ConsultFormMapper;
import com.community.communitycp.consult.service.ConsultFormService;
import com.community.communitycp.consult.vo.ConsultFormVO;
import com.community.communitycp.order.entity.ConsultationOrder;
import com.community.communitycp.order.mapper.ConsultationOrderMapper;
import org.springframework.stereotype.Service;

@Service
public class ConsultFormServiceImpl implements ConsultFormService {

    private final ConsultFormMapper consultFormMapper;
    private final ConsultationOrderMapper consultationOrderMapper;

    public ConsultFormServiceImpl(ConsultFormMapper consultFormMapper, ConsultationOrderMapper consultationOrderMapper) {
        this.consultFormMapper = consultFormMapper;
        this.consultationOrderMapper = consultationOrderMapper;
    }

    @Override
    public ConsultFormVO getConsultForm(Long orderId) {
        Long patientId = UserContext.getUserId();

        ConsultationOrder order = consultationOrderMapper.selectById(orderId);
        if (order == null || !patientId.equals(order.getPatientId())) {
            throw new BizException(ResultCode.CONSULTATION_ORDER_NOT_FOUND);
        }

        ConsultForm consultForm = consultFormMapper.selectOne(new LambdaQueryWrapper<ConsultForm>()
                .eq(ConsultForm::getOrderId, orderId)
        );
        //封装vo

        if (consultForm == null) {
            throw new BizException(ResultCode.CONSULT_FORM_NOT_FOUND);
        }
        ConsultFormVO vo = new ConsultFormVO();

        vo.setOrderId(orderId.toString());
        vo.setSymptom(consultForm.getSymptom());
        vo.setImgUrl(consultForm.getImgUrl());
        return vo;
    }

    @Override
    public void updateConsultForm(Long orderId, ConsultFormDTO dto) {
        Long patientId = UserContext.getUserId();

        ConsultationOrder order = consultationOrderMapper.selectById(orderId);
        if (order == null || !patientId.equals(order.getPatientId())) {
            throw new BizException(ResultCode.CONSULTATION_ORDER_NOT_FOUND);
        }

        if (order.getOrderStatus() == null || order.getOrderStatus() != OrderStatusConstant.PENDING_PAY) {
            throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 仅待支付阶段可改：症状必改，图片传 null 表示不改、传空串表示清空
        int rows = consultFormMapper.update(null, new LambdaUpdateWrapper<ConsultForm>()
                .eq(ConsultForm::getOrderId, orderId)
                .set(ConsultForm::getSymptom, dto.getSymptom())
                .set(dto.getImgUrl() != null, ConsultForm::getImgUrl, dto.getImgUrl())
        );
        if (rows == 0) {
            throw new BizException(ResultCode.CONSULT_FORM_NOT_FOUND);
        }
    }
}
