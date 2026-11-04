package com.community.communitycp.doctor.service;

import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.doctor.dto.DoctorQueryDTO;
import com.community.communitycp.doctor.vo.DoctorListVO;

/**
 * ClassName: DoctorService
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/2 15:29
 * Version:1.0
 */
public interface DoctorService {

    /**
     * 科室下医生列表（仅在线且空闲的医生，分页）
     */
    PageResult<DoctorListVO> getDoctorList(DoctorQueryDTO queryDTO);
}
