package com.community.communitycp.doctor.service;

import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.doctor.dto.DoctorQueryDTO;
import com.community.communitycp.doctor.dto.OnlineStatusDTO;
import com.community.communitycp.doctor.vo.DoctorDetailVO;
import com.community.communitycp.doctor.vo.DoctorListVO;
import com.community.communitycp.doctor.vo.DoctorProfileVO;
import jakarta.validation.Valid;

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

    /**
     * 医生详情（公开）
     *
     * @param userId 医生用户ID（即 user.id）
     */
    DoctorDetailVO getDoctorDetail(Long userId);

    /**
     * 医生查看自己的信息
     *
     * @param userId 当前登录医生的用户ID（从 Token 解析，不接受前端传参）
     */
    DoctorProfileVO getDoctorProfile(Long userId);

    /**
     * 更新医生状态（仅医生）
     */
    void updateOnlineStatus( OnlineStatusDTO onlineStatusDTO);
}
