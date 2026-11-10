package com.community.communitycp.consult.service;

import com.community.communitycp.consult.vo.PrescriptionVO;

/**
 * 处方服务
 */
public interface PrescriptionService {

    /**
     * 患者查询自己订单的处方
     */
    PrescriptionVO getPrescription(Long orderId);
}
