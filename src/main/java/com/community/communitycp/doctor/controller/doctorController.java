package com.community.communitycp.doctor.controller;

import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.doctor.dto.DoctorQueryDTO;
import com.community.communitycp.doctor.service.DoctorService;
import com.community.communitycp.doctor.vo.DoctorListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: doctorController
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/2 15:26
 * Version:1.0
 */
@RestController
@RequestMapping("/doctor")
@Tag(name = "医生接口")
public class doctorController {

    private final DoctorService doctorService;

    public doctorController(DoctorService doctorService) {
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
}
