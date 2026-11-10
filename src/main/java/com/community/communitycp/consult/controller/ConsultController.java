package com.community.communitycp.consult.controller;

import com.community.communitycp.common.constant.UserRoleConstant;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.web.RequireRole;
import com.community.communitycp.consult.dto.ConsultFormDTO;
import com.community.communitycp.consult.service.ConsultFormService;
import com.community.communitycp.consult.vo.ConsultFormVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: ConsultController
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/10 21:07
 * Version:1.0
 */
@Tag(name = "问诊接口")
@RestController
@RequestMapping("/consult-form")
public class ConsultController {

    private final ConsultFormService consultFormService;
    public ConsultController (ConsultFormService consultFormService) {
        this.consultFormService = consultFormService;
    }

    @RequireRole(UserRoleConstant.PATIENT)
    @GetMapping("/{orderId}")
    @Operation(summary = "根据订单ID查询问诊表单")
    public Result<ConsultFormVO> getConsultForm(@PathVariable Long orderId) {
        return Result.success(consultFormService.getConsultForm(orderId));
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "修改问诊表单")
    @RequireRole(UserRoleConstant.PATIENT)
    public Result<Void> updateConsultForm(@PathVariable Long orderId, @Valid @RequestBody ConsultFormDTO dto) {
        consultFormService.updateConsultForm(orderId, dto);
        return Result.success();
    }

}
