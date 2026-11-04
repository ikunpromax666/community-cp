package com.community.communitycp.doctor.controller;

import com.community.communitycp.common.core.Result;
import com.community.communitycp.doctor.service.DeptService;
import com.community.communitycp.doctor.vo.DeptVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/department")
@Tag(name = "科室接口")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @Operation(summary = "科室列表（公开）")
    @GetMapping("/list")
    public Result<List<DeptVO>> list() {
        return Result.success(deptService.list());
    }
}
