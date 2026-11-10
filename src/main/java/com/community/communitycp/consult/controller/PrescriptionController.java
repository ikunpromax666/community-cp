package com.community.communitycp.consult.controller;

import com.community.communitycp.common.constant.UserRoleConstant;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.web.RequireRole;
import com.community.communitycp.consult.service.PrescriptionService;
import com.community.communitycp.consult.vo.PrescriptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处方接口
 */
@RestController
@RequestMapping("/prescription")
@Tag(name = "处方接口")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * 患者查看自己订单的处方
     */
    @Operation(summary = "查看处方")
    @RequireRole(UserRoleConstant.PATIENT)
    @GetMapping("/{orderId}")
    public Result<PrescriptionVO> getPrescription(@PathVariable Long orderId) {
        return Result.success(prescriptionService.getPrescription(orderId));
    }
}
