package com.community.communitycp.consult.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.communitycp.common.core.BizException;
import com.community.communitycp.common.core.ResultCode;
import com.community.communitycp.common.web.UserContext;
import com.community.communitycp.consult.entity.Prescription;
import com.community.communitycp.consult.mapper.PrescriptionMapper;
import com.community.communitycp.consult.service.PrescriptionService;
import com.community.communitycp.consult.vo.PrescriptionVO;
import com.community.communitycp.order.entity.ConsultationOrder;
import com.community.communitycp.order.mapper.ConsultationOrderMapper;
import com.community.communitycp.user.entity.User;
import com.community.communitycp.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * 处方服务实现
 */
@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionMapper prescriptionMapper;
    private final ConsultationOrderMapper consultationOrderMapper;
    private final UserMapper userMapper;

    public PrescriptionServiceImpl(PrescriptionMapper prescriptionMapper,
                                   ConsultationOrderMapper consultationOrderMapper,
                                   UserMapper userMapper) {
        this.prescriptionMapper = prescriptionMapper;
        this.consultationOrderMapper = consultationOrderMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PrescriptionVO getPrescription(Long orderId) {
        Long patientId = UserContext.getUserId();

        // 1. 查订单并校验归属：不匹配一律按「订单不存在」处理，避免枚举他人订单
        ConsultationOrder order = consultationOrderMapper.selectById(orderId);
        if (order == null || !order.getPatientId().equals(patientId)) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 查处方（order_id 有唯一索引，一单一处方）
        Prescription prescription = prescriptionMapper.selectOne(
                new LambdaQueryWrapper<Prescription>().eq(Prescription::getOrderId, orderId));
        if (prescription == null) {
            throw new BizException(ResultCode.PRESCRIPTION_NOT_FOUND);
        }

        // 3. 医生姓名存在 user.real_name，按 user.id 查
        User doctorUser = userMapper.selectById(prescription.getDoctorId());

        // 4. 组装返回
        PrescriptionVO vo = new PrescriptionVO();
        vo.setPrescriptionId(String.valueOf(prescription.getId()));
        vo.setOrderId(String.valueOf(orderId));
        vo.setDoctorName(doctorUser != null ? doctorUser.getRealName() : null);
        vo.setPrescriptionNo(prescription.getPrescriptionNo());
        vo.setAdvice(prescription.getAdvice());
        vo.setDrugInfo(prescription.getDrugInfo());
        vo.setCreateTime(prescription.getCreateTime());
        return vo;
    }
}
