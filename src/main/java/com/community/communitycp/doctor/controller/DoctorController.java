package com.community.communitycp.doctor.controller;

import com.community.communitycp.common.constant.UserRoleConstant;
import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.web.RequireRole;
import com.community.communitycp.common.web.UserContext;
import com.community.communitycp.doctor.dto.DoctorQueryDTO;
import com.community.communitycp.doctor.dto.OnlineStatusDTO;
import com.community.communitycp.doctor.service.DoctorService;
import com.community.communitycp.doctor.vo.DoctorDetailVO;
import com.community.communitycp.doctor.vo.DoctorListVO;
import com.community.communitycp.doctor.vo.DoctorProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: DoctorController
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/2 15:26
 * Version:1.0
 */
@RestController
@RequestMapping("/doctor")
@Tag(name = "医生接口")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /**
     * GET 请求参数走 Query String，不能用 @RequestBody（GET 请求体不被保证传递）
     */
    @Operation(summary = "科室下医生列表（公开）")
    @GetMapping("/list")
    public Result<PageResult<DoctorListVO>> getDoctorList(@Valid DoctorQueryDTO queryDTO) {
        return Result.success(doctorService.getDoctorList(queryDTO));
    }

    /**
     * 详情路径用 /detail/{id}，正好命中 WebMvcConfig 白名单里的 /doctor/detail/**
     */
    @Operation(summary = "医生详情（公开）")
    @GetMapping("/detail/{id}")
    public Result<DoctorDetailVO> getDoctorDetail(@PathVariable Long id) {
        return Result.success(doctorService.getDoctorDetail(id));
    }

    /**
     * 医生看自己的信息：userId 从 Token 解析，不接受前端传参，避免越权查看他人资料
     */
    @Operation(summary = "医生查看自己的信息（仅医生）")
    @RequireRole(UserRoleConstant.DOCTOR)
    @GetMapping("/profile")
    public Result<DoctorProfileVO> getDoctorProfile() {
        return Result.success(doctorService.getDoctorProfile(UserContext.getUserId()));
    }

    /**
     * 医生切换自己的在线状态：PUT + JSON Body，必须加 @RequestBody，
     * 否则 Spring 会按表单/URL 参数绑定，JSON 里的 onlineStatus 读不进来。
     */
    @Operation(summary = "医生切换在线状态（仅医生）")
    @RequireRole(UserRoleConstant.DOCTOR)
    @PutMapping("/online-status")
    public Result<Void> updateOnlineStatus(@Valid @RequestBody OnlineStatusDTO onlineStatusDTO) {
        doctorService.updateOnlineStatus(onlineStatusDTO);
        return Result.success();
    }

}
